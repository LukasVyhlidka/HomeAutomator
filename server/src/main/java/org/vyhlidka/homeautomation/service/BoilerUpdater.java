package org.vyhlidka.homeautomation.service;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.vyhlidka.homeautomation.domain.Boiler;
import org.vyhlidka.homeautomation.repo.BoilerRepository;
import org.vyhlidka.homeautomation.repo.ElementNotFoundExcepion;

/**
 * Created by lucky on 18.12.16.
 */
public class BoilerUpdater {

    private static final Logger logger = LoggerFactory.getLogger(BoilerUpdater.class);

    private final String boilerId;
    private final BoilerRepository boilerRepository;

    public BoilerUpdater(final String boilerId, final BoilerRepository boilerRepository) {
        Validate.notNull(boilerId, "boilerId can not be null;");
        Validate.notNull(boilerRepository, "boilerRepository can not be null;");

        this.boilerId = boilerId;
        this.boilerRepository = boilerRepository;
    }

    @Scheduled(fixedRate = 1 * 60 * 1000, initialDelay = 0)
    public void updateBoiler() {
        try {
            Boiler b = this.boilerRepository.getBoiler(this.boilerId);

            b.setState(b.getState() == Boiler.BoilerState.SWITCHED_ON
                    ? Boiler.BoilerState.SWITCHED_OFF
                    : Boiler.BoilerState.SWITCHED_ON);

            this.boilerRepository.setBoiler(b);

            logger.info("Updated Boiler [{}]", b.getId());
        } catch (ElementNotFoundExcepion e) {
            logger.info("Repository does not contain the Boiler, creating one.");
            Boiler b = new Boiler();
            b.setState(Boiler.BoilerState.SWITCHED_OFF);
            b.setId(this.boilerId);
            this.boilerRepository.setBoiler(b);
        }
    }

}
