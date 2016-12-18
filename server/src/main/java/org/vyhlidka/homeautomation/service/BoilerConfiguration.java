package org.vyhlidka.homeautomation.service;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.vyhlidka.homeautomation.repo.BoilerRepository;

/**
 * Created by lucky on 18.12.16.
 */
@Configuration
public class BoilerConfiguration {

    private final BoilerRepository boilerRepository;

    @Autowired
    public BoilerConfiguration(final BoilerRepository boilerRepository) {
        Validate.notNull(boilerRepository, "boilerRepository can not be null;");

        this.boilerRepository = boilerRepository;
    }

    @Bean
    public BoilerUpdater createUpdater() {
        return new BoilerUpdater("TestBoiler", this.boilerRepository);
    }
}
