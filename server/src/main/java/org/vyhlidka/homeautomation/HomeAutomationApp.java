package org.vyhlidka.homeautomation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by lucky on 18.12.16.
 */
@SpringBootApplication
@EnableScheduling
public class HomeAutomationApp {

    public static void main(String[] args) {
        SpringApplication.run(HomeAutomationApp.class, args);
    }

}
