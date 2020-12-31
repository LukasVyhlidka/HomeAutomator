package org.vyhlidka.homeautomation.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
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

    private static final LocalDate yesterday = LocalDate.now().minus(1, ChronoUnit.DAYS);

    @Test
    public void testWholeDayOnWithOnOnly() throws Exception {
        List<BoilerChange> changes = Arrays.asList(
                new BoilerChange("b1", yesterday.atStartOfDay(), Boiler.BoilerState.SWITCHED_ON)
        );

        final int[] stats = BoilerStatistics.getDayStatistics(changes, yesterday);

        Assertions.assertThat(stats).hasSize(24).containsOnly(100);
    }

    @Test
    public void testWholeDayOn() throws Exception {
        List<BoilerChange> changes = Arrays.asList(
                new BoilerChange("b1", yesterday.atStartOfDay(), Boiler.BoilerState.SWITCHED_ON),
                new BoilerChange("b1", yesterday.atStartOfDay().plus(1, ChronoUnit.DAYS), Boiler.BoilerState.SWITCHED_OFF)
        );

        final int[] stats = BoilerStatistics.getDayStatistics(changes, yesterday);

        Assertions.assertThat(stats).hasSize(24).containsOnly(100);
    }

    @Test
    public void testSwitching() throws Exception {
        List<BoilerChange> changes = Arrays.asList(
                new BoilerChange("b1", yesterday.atStartOfDay(), Boiler.BoilerState.SWITCHED_OFF),
                new BoilerChange("b1", yesterday.atTime(6, 45), Boiler.BoilerState.SWITCHED_ON),
                new BoilerChange("b1", yesterday.atTime(8, 30), Boiler.BoilerState.SWITCHED_OFF),
                new BoilerChange("b1", yesterday.atTime(17, 10), Boiler.BoilerState.SWITCHED_ON)
        );

        final int[] stats = BoilerStatistics.getDayStatistics(changes, yesterday);

        Assertions.assertThat(stats).hasSize(24).
                containsExactly(
                        0, 0, 0, 0, 0, 0,
                        25, 100, 50,
                        0, 0, 0, 0, 0, 0, 0, 0,
                        83, 100, 100, 100, 100, 100, 100
                );
    }

    @Test
    public void testVisualize() throws Exception {
        int[] stats = new int[24];
        Arrays.fill(stats, 15);

        stats[7] = 100;
        stats[8] = 55;

        String res = BoilerStatistics.visualizeStatistics(stats);
        System.out.println(res);
    }
}