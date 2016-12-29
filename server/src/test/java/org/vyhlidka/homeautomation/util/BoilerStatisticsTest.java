package org.vyhlidka.homeautomation.util;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.vyhlidka.homeautomation.domain.Boiler;
import org.vyhlidka.homeautomation.domain.BoilerChange;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lucky on 29.12.16.
 */
public class BoilerStatisticsTest {

    @Test
    public void testWholeDayOnWithOnOnly() throws Exception {
        List<BoilerChange> changes = Arrays.asList(
                new BoilerChange("b1", LocalDate.now().atStartOfDay(), Boiler.BoilerState.SWITCHED_ON)
        );

        final int[] stats = BoilerStatistics.getDayStatistics(changes, LocalDate.now());

        Assertions.assertThat(stats).hasSize(24).containsOnly(100);
    }

    @Test
    public void testWholeDayOn() throws Exception {
        List<BoilerChange> changes = Arrays.asList(
                new BoilerChange("b1", LocalDate.now().atStartOfDay(), Boiler.BoilerState.SWITCHED_ON),
                new BoilerChange("b1", LocalDate.now().atStartOfDay().plus(1, ChronoUnit.DAYS), Boiler.BoilerState.SWITCHED_OFF)
        );

        final int[] stats = BoilerStatistics.getDayStatistics(changes, LocalDate.now());

        Assertions.assertThat(stats).hasSize(24).containsOnly(100);
    }

    @Test
    public void testSwitching() throws Exception {
        List<BoilerChange> changes = Arrays.asList(
                new BoilerChange("b1", LocalDate.now().atStartOfDay(), Boiler.BoilerState.SWITCHED_OFF),
                new BoilerChange("b1", LocalDate.now().atTime(6, 45), Boiler.BoilerState.SWITCHED_ON),
                new BoilerChange("b1", LocalDate.now().atTime(8, 30), Boiler.BoilerState.SWITCHED_OFF),
                new BoilerChange("b1", LocalDate.now().atTime(17, 10), Boiler.BoilerState.SWITCHED_ON)
        );

        final int[] stats = BoilerStatistics.getDayStatistics(changes, LocalDate.now());

        Assertions.assertThat(stats).hasSize(24).
                containsExactly(
                        0, 0, 0, 0, 0, 0,
                        25, 100, 50,
                        0, 0, 0, 0, 0, 0, 0, 0,
                        83, 100, 100, 100, 100, 100, 100
                );
    }
}