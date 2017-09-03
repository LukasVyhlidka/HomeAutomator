package org.vyhlidka.homeautomation.service;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.vyhlidka.homeautomation.eq3.CubeClient;
import org.vyhlidka.homeautomation.repo.BoilerChangeRepository;
import org.vyhlidka.homeautomation.repo.BoilerRepository;
import org.vyhlidka.homeautomation.repo.RoomRepository;

/**
 * Created by lucky on 18.12.16.
 */
@Configuration
public class BoilerConfiguration {

    private final BoilerRepository boilerRepository;
    private final CubeClient cubeClient;
    private final BoilerChangeRepository changeRepository;
    private final RoomRepository roomRepository;

    @Autowired
    public BoilerConfiguration(final BoilerRepository boilerRepository, final CubeClient cubeClient, final BoilerChangeRepository changeRepository,
                               final RoomRepository roomRepository) {
        Validate.notNull(boilerRepository, "boilerRepository can not be null;");
        Validate.notNull(cubeClient, "cubeClient can not be null;");
        Validate.notNull(changeRepository, "changeRepository can not be null;");
        Validate.notNull(roomRepository, "roomRepository can not be null;");

        this.boilerRepository = boilerRepository;
        this.cubeClient = cubeClient;
        this.changeRepository = changeRepository;
        this.roomRepository = roomRepository;
    }

    @Bean
    public BoilerUpdater createUpdater() {
        return new BoilerUpdater("TestBoiler", this.cubeClient, this.changeRepository, this.boilerRepository, this.roomRepository);
    }
}
