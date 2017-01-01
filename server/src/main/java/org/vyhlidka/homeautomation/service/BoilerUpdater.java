package org.vyhlidka.homeautomation.service;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.vyhlidka.homeautomation.domain.Boiler;
import org.vyhlidka.homeautomation.domain.BoilerChange;
import org.vyhlidka.homeautomation.eq3.CubeClient;
import org.vyhlidka.homeautomation.eq3.domain.LMaxMessage;
import org.vyhlidka.homeautomation.repo.BoilerChangeRepository;
import org.vyhlidka.homeautomation.repo.BoilerRepository;
import org.vyhlidka.homeautomation.repo.ElementNotFoundExcepion;
import org.vyhlidka.homeautomation.util.BoilerStatistics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lucky on 18.12.16.
 */
public class BoilerUpdater {

    private static final Logger logger = LoggerFactory.getLogger(BoilerUpdater.class);
    private static final String statsFolder = "data/statistics/";
    private static final double threshold = 0.2;

    private final String boilerId;
    private final CubeClient cubeClient;

    private final BoilerChangeRepository changeRepository;
    private final BoilerRepository boilerRepository;

    public BoilerUpdater(final String boilerId, final CubeClient cubeClient, final BoilerChangeRepository changeRepository, final BoilerRepository boilerRepository) {
        Validate.notNull(boilerId, "boilerId can not be null;");
        Validate.notNull(cubeClient, "cubeClient can not be null;");
        Validate.notNull(changeRepository, "changeRepository can not be null;");
        Validate.notNull(boilerRepository, "boilerRepository can not be null;");

        this.boilerId = boilerId;
        this.cubeClient = cubeClient;
        this.changeRepository = changeRepository;
        this.boilerRepository = boilerRepository;
    }

    @Scheduled(fixedRate = 1 * 60 * 1000, initialDelay = 0)
    public void updateBoiler() {
        logger.debug("Update Boiler start [{}]", this.boilerId);

        Boiler b;
        //Create boiler if it is not in the repo
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

        Boiler.BoilerState newState = this.figureNewBoilerState(b);
        if (newState != b.getState()) {
            b.setState(newState);
            this.boilerRepository.setBoiler(b);
            this.changeRepository.addChange(new BoilerChange(this.boilerId, newState));
            logger.info("Updated Boiler [{}] to state [{}]", b.getId(), b.getState());
        }

        logger.debug("Update Boiler end [{}]", b.getId());
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

    private Boiler.BoilerState figureNewBoilerState(Boiler boiler) {
        Boiler.BoilerState actState = boiler.state;

        LMaxMessage deviceList = this.cubeClient.getDeviceList();

        final boolean anyThermostat = deviceList.devices.stream()
                .filter(d -> d.type == LMaxMessage.MaxDeviceType.THERMOSTAT)
                .anyMatch(p -> true);

        // either there is an ON-Thermostat or there are not thermostats at all.
        final boolean thermostatOn = !anyThermostat || deviceList.devices.stream()
                .filter(d -> d.type == LMaxMessage.MaxDeviceType.THERMOSTAT)
                .anyMatch(t -> {
                    double actualTemp = (double) t.actualTemperature / 10;
                    double temp = (double) t.temperature / 2;

                    //Either it is too cold (temp - threshold) or it is heating at present and not too hot (temp + threshold).
                    return actualTemp <= (temp - threshold)
                            || (actState == Boiler.BoilerState.SWITCHED_ON && actualTemp < (temp + threshold));
                });

        final boolean anyValve = deviceList.devices.stream()
                .filter(d -> d.type == LMaxMessage.MaxDeviceType.VALVE)
                .anyMatch(p -> true);

        // Either there is at least one opened valve or there are no valves at all.
        boolean valveOpened = !anyValve || deviceList.devices.stream()
                .filter(d -> d.type == LMaxMessage.MaxDeviceType.VALVE)
                .anyMatch(v -> v.valvePosition > 0);

        boolean on = thermostatOn && valveOpened;

        return on ? Boiler.BoilerState.SWITCHED_ON : Boiler.BoilerState.SWITCHED_OFF;
    }

}
