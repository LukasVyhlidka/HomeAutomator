package org.vyhlidka.homeautomation.service;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.vyhlidka.homeautomation.domain.Boiler;
import org.vyhlidka.homeautomation.domain.BoilerChange;
import org.vyhlidka.homeautomation.domain.Room;
import org.vyhlidka.homeautomation.eq3.CubeClient;
import org.vyhlidka.homeautomation.eq3.domain.LMaxMessage;
import org.vyhlidka.homeautomation.eq3.domain.MMaxMessage;
import org.vyhlidka.homeautomation.eq3.domain.MaxMessage;
import org.vyhlidka.homeautomation.repo.BoilerChangeRepository;
import org.vyhlidka.homeautomation.repo.BoilerRepository;
import org.vyhlidka.homeautomation.repo.ElementNotFoundExcepion;
import org.vyhlidka.homeautomation.repo.RoomRepository;
import org.vyhlidka.homeautomation.util.BoilerStatistics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by lucky on 18.12.16.
 */
public class BoilerUpdater {

    private static final Logger logger = LoggerFactory.getLogger(BoilerUpdater.class);
    private static final String statsFolder = "data/statistics/";
    private static final double threshold = 0.20;

    private final String boilerId;
    private final CubeClient cubeClient;

    private final BoilerChangeRepository changeRepository;
    private final BoilerRepository boilerRepository;
    private final RoomRepository roomRepository;

    private LocalDateTime lastUpdateTime = LocalDateTime.now();

    public BoilerUpdater(final String boilerId, final CubeClient cubeClient, final BoilerChangeRepository changeRepository,
                         final BoilerRepository boilerRepository, final RoomRepository roomRepository) {
        Validate.notNull(boilerId, "boilerId can not be null;");
        Validate.notNull(cubeClient, "cubeClient can not be null;");
        Validate.notNull(changeRepository, "changeRepository can not be null;");
        Validate.notNull(boilerRepository, "boilerRepository can not be null;");
        Validate.notNull(roomRepository, "roomRepository can not be null;");

        this.boilerId = boilerId;
        this.cubeClient = cubeClient;
        this.changeRepository = changeRepository;
        this.boilerRepository = boilerRepository;
        this.roomRepository = roomRepository;
    }

    public LocalDateTime getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    @Scheduled(fixedRate = 1 * 60 * 1000, initialDelay = 0)
    public void updateBoiler() {
        logger.debug("Update Boiler start [{}]", this.boilerId);

        this.updateRooms();

        Boiler b;
        // Create boiler if it is not in the repo
        try {
            b = this.boilerRepository.getBoiler(this.boilerId);
        } catch (ElementNotFoundExcepion ex) {
            logger.info("Repository does not contain the Boiler, creating one.");
            b = new Boiler();
            b.setState(Boiler.BoilerState.SWITCHED_OFF);
            b.setId(this.boilerId);
            this.boilerRepository.setBoiler(b);

            this.changeRepository.addChange(new BoilerChange(this.boilerId, b.getState()));
        }

        // Is there a room that needs to heat OR no rooms at all?
        final List<Room> rooms = this.roomRepository.getRooms();
        boolean heatOn = rooms.isEmpty() || rooms.stream().anyMatch(r -> r.state == Boiler.BoilerState.SWITCHED_ON);

        Boiler.BoilerState newState = heatOn ? Boiler.BoilerState.SWITCHED_ON : Boiler.BoilerState.SWITCHED_OFF;
        if (newState != b.getState()) {
            b.setState(newState);
            this.boilerRepository.setBoiler(b);
            this.changeRepository.addChange(new BoilerChange(this.boilerId, newState));
            logger.info("Updated Boiler [{}] to state [{}]", b.getId(), b.getState());
        }

        this.lastUpdateTime = LocalDateTime.now();

        logger.debug("Update Boiler end [{}]", b.getId());
    }

    public void updateRooms() {
        logger.debug("Update Rooms started.");

        final List<MaxMessage> initialMessages = this.cubeClient.getInitialMessages();
        LMaxMessage lMessage = (LMaxMessage) initialMessages.stream()
                .filter(m -> m instanceof LMaxMessage)
                .findFirst()
                .get();
        MMaxMessage mMessage = (MMaxMessage) initialMessages.stream()
                .filter(m -> m instanceof MMaxMessage)
                .findFirst()
                .orElseGet(() -> new MMaxMessage("X", -1, 0, Collections.emptyList(), Collections.emptyList()));

        final Map<MMaxMessage.MaxRoomMeta, List<LMaxMessage.MaxDevice>> roomMap = this.groupDevices(lMessage, mMessage);
        final Set<Integer> roomIdMetas = roomMap.keySet().stream().map(m -> m.id).collect(Collectors.toSet());

        final List<Room> rooms = this.roomRepository.getRooms();
        final Set<Integer> roomsToDel = rooms.stream().map(r -> r.id).filter(r -> !roomIdMetas.contains(r)).collect(Collectors.toSet());

        for (Map.Entry<MMaxMessage.MaxRoomMeta, List<LMaxMessage.MaxDevice>> roomEntry : roomMap.entrySet()) {
            logger.debug("Processing room {}", roomEntry.getKey().name);

            Room room = this.roomRepository.getRoom(roomEntry.getKey().id);
            if (room == null) {
                room = new Room(roomEntry.getKey().id, roomEntry.getKey().name, Boiler.BoilerState.SWITCHED_ON);
            }

            final Boiler.BoilerState newState = this.figureNewBoilerState(room.state, roomEntry.getValue());

            logger.debug("Room [{}] state is {}", roomEntry.getKey().name, newState);
            this.roomRepository.saveRoom(new Room(room.id, room.name, newState));
        }

        logger.debug("Deleting [{}] rooms", roomsToDel.size());
        roomsToDel.forEach(rtd -> this.roomRepository.deleteRoom(rtd));
        logger.debug("Rooms updated.");
    }

    @Scheduled(cron = "0 59 23 * * *")
    public void printAndClearStatistics() {
        final List<BoilerChange> changes = this.changeRepository.getChanges();

        this.changeRepository.clear();

        Pair<Long, Long> onOffTimes = BoilerStatistics.getOnOffTimes(changes);
        long timeOn = onOffTimes.getLeft();
        long timeOff = onOffTimes.getRight();
        logger.info("Boiler Statistics for day {}: \n\tOn Time: {} seconds\n\tOff Time: {} seconds\n\tOn Ratio: {}",
                LocalDate.now(), timeOn, timeOff, (double) timeOn / (timeOn + timeOff));

        int[] dayStats = BoilerStatistics.getDayStatistics(changes, LocalDate.now());
        logger.info("Boiler Hour Statistics for day {}: {}",
                LocalDate.now(), Arrays.toString(dayStats));

        String visualStats = BoilerStatistics.visualizeStatistics(dayStats);
        logger.info("visualization: \n" + visualStats);

        File statsDir = new File(statsFolder);
        statsDir.mkdirs();

        File f = new File(statsDir, LocalDate.now().toString() + ".log");
        try (FileWriter fw = new FileWriter(f)) {
            fw.write(visualStats);
        } catch (IOException ex) {
            logger.error("Error during Statistics file output.", ex);
        }
    }

    private Boiler.BoilerState figureNewBoilerState(Boiler.BoilerState actState, List<LMaxMessage.MaxDevice> devices) {
        Validate.notNull(actState, "devices can not be null;");
        Validate.notNull(devices, "devices can not be null;");

        final boolean anyThermostat = devices.stream()
                .filter(d -> d.type == LMaxMessage.MaxDeviceType.THERMOSTAT)
                .anyMatch(p -> true);

        // either there is an ON-Thermostat or there are not thermostats at all.
        final boolean thermostatOn = !anyThermostat || devices.stream()
                .filter(d -> d.type == LMaxMessage.MaxDeviceType.THERMOSTAT)
                .anyMatch(t -> {
                    double actualTemp = (double) t.actualTemperature / 10;
                    double temp = (double) t.temperature / 2;

                    //Either it is too cold (temp - threshold) or it is heating at present and not too hot (temp + threshold).
                    return actualTemp <= (temp - threshold)
                            || (actState == Boiler.BoilerState.SWITCHED_ON && actualTemp < (temp + threshold));
                });

        final boolean anyValve = devices.stream()
                .filter(d -> d.type == LMaxMessage.MaxDeviceType.VALVE)
                .anyMatch(p -> true);

        // Either there is at least one opened valve or there are no valves at all.
        boolean valveOpened = !anyValve || devices.stream()
                .filter(d -> d.type == LMaxMessage.MaxDeviceType.VALVE)
                .anyMatch(v -> v.valvePosition > 10);

        boolean on = thermostatOn && valveOpened;

        return on ? Boiler.BoilerState.SWITCHED_ON : Boiler.BoilerState.SWITCHED_OFF;
    }

    private static final MMaxMessage.MaxRoomMeta DEFAULT_ROOM = new MMaxMessage.MaxRoomMeta(-1, "Default", -1);
    private Map<MMaxMessage.MaxRoomMeta, List<LMaxMessage.MaxDevice>> groupDevices(LMaxMessage lMaxMessage, MMaxMessage mMaxMessage) {
        Map<MMaxMessage.MaxRoomMeta, List<LMaxMessage.MaxDevice>> res = new HashMap<>();

        for (LMaxMessage.MaxDevice device : lMaxMessage.devices) {
            final Optional<MMaxMessage.MaxDeviceMeta> deviceMeta = mMaxMessage.devices.stream().filter(meta -> device.rfAddress == meta.rfAddress).findFirst();
            if (! deviceMeta.isPresent()) {
                logger.warn("DeviceMeta for device [{}] was not found.", device.rfAddress);
                res.computeIfAbsent(DEFAULT_ROOM, key -> new ArrayList<>());
                res.get(DEFAULT_ROOM).add(device);
                continue;
            }

            final Optional<MMaxMessage.MaxRoomMeta> roomMeta = mMaxMessage.rooms.stream().filter(meta -> meta.id == deviceMeta.get().roomId).findFirst();
            if (! deviceMeta.isPresent()) {
                logger.warn("Room for device [{}] was not found.", deviceMeta.get().name);
                res.computeIfAbsent(DEFAULT_ROOM, key -> new ArrayList<>());
                res.get(DEFAULT_ROOM).add(device);
                continue;
            }

            //Add the device into the map
            res.computeIfAbsent(roomMeta.get(), key -> new ArrayList<>());
            res.get(roomMeta.get()).add(device);
        }

        return res;
    }

}
