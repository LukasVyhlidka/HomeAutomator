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
import org.vyhlidka.homeautomation.util.IterableUtil;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

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
        logger.debug("Update Boiler start [{}]", this.boilerId);

        //Create boiler if it is not in the repo
        try {
            Boiler b = this.boilerRepository.getBoiler(this.boilerId);
        } catch (ElementNotFoundExcepion ex) {
            logger.info("Repository does not contain the Boiler, creating one.");
            Boiler b = new Boiler();
            b.setState(Boiler.BoilerState.SWITCHED_OFF);
            b.setId(this.boilerId);
            this.boilerRepository.setBoiler(b);

            this.changeRepository.addChange(new BoilerChange(this.boilerId, b.getState()));
        }

        Boiler b = this.boilerRepository.getBoiler(this.boilerId);
        Boiler.BoilerState actualState = this.figureBoilerState();
        if (actualState != b.getState()) {
            b.setState(actualState);
            this.boilerRepository.setBoiler(b);
            this.changeRepository.addChange(new BoilerChange(this.boilerId, actualState));
            logger.info("Updated Boiler [{}] to state [{}]", b.getId(), b.getState());
        }

        logger.debug("Update Boiler end [{}]", b.getId());
    }

    @Scheduled(cron = "0 59 23 * * *")
    public void printAndClearStatistics() {
        long timeOn = 0;
        long timeOff = 0;
        BoilerChange prevChange = null;

        Iterable<BoilerChange> changeIterable = IterableUtil.concat(
                this.changeRepository.getChanges(),
                Arrays.asList(new BoilerChange(this.boilerId, Boiler.BoilerState.SWITCHED_OFF)));
        for (BoilerChange change : changeIterable) {
            if (prevChange != null) {
                long delta = ChronoUnit.SECONDS.between(prevChange.dateTime, change.dateTime);
                if (prevChange.state == Boiler.BoilerState.SWITCHED_ON) {
                    timeOn += delta;
                } else {
                    timeOff += delta;
                }
            }

            prevChange = change;
        }

        this.changeRepository.clear();

        logger.info("Boiler Statistics for day {}: \n\tOn Time: {} seconds\n\tOff Time: {} seconds\n\tOn Ratio: {}",
                LocalDate.now(), timeOn, timeOff, (double) timeOn / (timeOn + timeOff));
    }

    private Boiler.BoilerState figureBoilerState() {
        LMaxMessage deviceList = this.cubeClient.getDeviceList();

        boolean on = deviceList.devices.stream()
                .filter(d -> d.type == LMaxMessage.MaxDeviceType.THERMOSTAT)
                .anyMatch(t -> ((double) t.actualTemperature / 10) < (((double) t.temperature / 2) + 0.2));

        return on ? Boiler.BoilerState.SWITCHED_ON : Boiler.BoilerState.SWITCHED_OFF;
    }

}
