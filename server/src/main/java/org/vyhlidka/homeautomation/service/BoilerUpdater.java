package org.vyhlidka.homeautomation.service;

import org.apache.commons.lang3.Validate;
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
import org.vyhlidka.homeautomation.util.Box;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

/**
 * Created by lucky on 18.12.16.
 */
public class BoilerUpdater {

    private static final Logger logger = LoggerFactory.getLogger(BoilerUpdater.class);

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
        try {
            logger.debug("Update Boiler start [{}]", this.boilerId);
            Boiler b = this.boilerRepository.getBoiler(this.boilerId);

            Boiler.BoilerState actualState = this.figureBoilerState();
            if (actualState != b.getState()) {
                b.setState(actualState);
                this.boilerRepository.setBoiler(b);
                this.changeRepository.addChange(new BoilerChange(LocalDateTime.now(), actualState));
                logger.info("Updated Boiler [{}] to state [{}]", b.getId(), b.getState());
            }

            /*b.setState(b.getState() == Boiler.BoilerState.SWITCHED_ON
                    ? Boiler.BoilerState.SWITCHED_OFF
                    : Boiler.BoilerState.SWITCHED_ON);

            this.boilerRepository.setBoiler(b);*/

            logger.debug("Update Boiler end [{}]", b.getId());
        } catch (ElementNotFoundExcepion e) {
            logger.info("Repository does not contain the Boiler, creating one.");
            Boiler b = new Boiler();
            b.setState(Boiler.BoilerState.SWITCHED_OFF);
            b.setId(this.boilerId);
            this.boilerRepository.setBoiler(b);
        }
    }

    @Scheduled(cron = "0 59 23 * * *")
    public void printAndClearStatistics() {
        Box<Long> timeOnBox = new Box<>(0L);
        Box<Long> timeOffBox = new Box<>(0L);
        Box<BoilerChange> prevChangeStore = new Box<>();
        Stream.concat(this.changeRepository.getChanges().stream(), Stream.of(new BoilerChange(Boiler.BoilerState.SWITCHED_OFF)))
                .forEach(change -> {
                    if (prevChangeStore.contains()) {
                        long delta = ChronoUnit.SECONDS.between(change.dateTime, prevChangeStore.get().dateTime);
                        if (prevChangeStore.get().state == Boiler.BoilerState.SWITCHED_ON) {
                            timeOnBox.set(timeOnBox.get() + delta);
                        } else {
                            timeOnBox.set(timeOffBox.get() + delta);
                        }
                    }

                    prevChangeStore.set(change);
                });

        this.changeRepository.clear();

        logger.info("Boiler Statistics for day {}: \n\tOn Time: {} seconds\n\tOff Time: {} seconds\n\tOn Ratio: {}",
                LocalDate.now(), timeOnBox.get(), timeOffBox.get(), (double)timeOnBox.get() / (timeOffBox.get() + timeOnBox.get()));
    }

    private Boiler.BoilerState figureBoilerState() {
        LMaxMessage deviceList = this.cubeClient.getDeviceList();

        boolean on = deviceList.devices.stream()
                .filter(d -> d.type == LMaxMessage.MaxDeviceType.THERMOSTAT)
                .anyMatch(t -> ((double) t.actualTemperature / 10) < (((double) t.temperature / 2) + 0.2));

        return on ? Boiler.BoilerState.SWITCHED_ON : Boiler.BoilerState.SWITCHED_OFF;
    }

}
