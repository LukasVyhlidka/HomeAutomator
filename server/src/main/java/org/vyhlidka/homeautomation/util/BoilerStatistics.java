package org.vyhlidka.homeautomation.util;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.vyhlidka.homeautomation.domain.Boiler;
import org.vyhlidka.homeautomation.domain.BoilerChange;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by lucky on 29.12.16.
 */
public class BoilerStatistics {

    public static Pair<Long, Long> getOnOffTimes(List<BoilerChange> changes) {
        long timeOn = 0;
        long timeOff = 0;
        BoilerChange prevChange = null;

        Iterable<BoilerChange> changeIterable = IterableUtil.concat(
                changes,
                Arrays.asList(new BoilerChange("fake id", Boiler.BoilerState.SWITCHED_OFF)));
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

        return Pair.of(timeOn, timeOff);
    }

    public static int[] getDayStatistics(List<BoilerChange> changes, LocalDate day) {
        Validate.notNull(changes, "changes can not be null;");
        Validate.notNull(day, "day can not be null;");

        LocalDateTime dayStart = day.atStartOfDay();
        LocalDateTime dayEnd = dayStart.with(LocalTime.MAX);

        changes.sort(Comparator.comparing(ch -> ch.dateTime));
        LocalDateTime lastChangeTime = changes.isEmpty() ? dayEnd : changes.get(changes.size() - 1).dateTime;

        int[] hourStat = new int[24];
        Arrays.fill(hourStat, 0);

        // Add one change after the day end to calculate the last boiler state.
        Iterable<BoilerChange> changeIterable = IterableUtil.concat(
                changes,
                Arrays.asList(new BoilerChange("fake id", lastChangeTime.plus(1, ChronoUnit.DAYS), Boiler.BoilerState.SWITCHED_OFF)));

        BoilerChange prevChange = null;
        for (BoilerChange change : changeIterable) {
            if (prevChange != null) {
                if (prevChange.state == Boiler.BoilerState.SWITCHED_OFF) {
                    // This range is OFF state, it is not calculated.
                    prevChange = change;
                    continue;
                }

                // Range is before this day. Continue
                if (change.dateTime.isBefore(dayStart)) {
                    prevChange = change;
                    continue;
                }

                // The range is after. Terminate
                if (prevChange.dateTime.isAfter(dayEnd)) {
                    break;
                }

                //The range is somewhere in our day... Alter hours.
                for (int i = 0; i < hourStat.length; i++) {
                    LocalDateTime hourStart = dayStart.withHour(i);
                    LocalDateTime hourEnd = hourStart.plus(1, ChronoUnit.HOURS).minus(1, ChronoUnit.NANOS);

                    int rangeOverlap = (int) Math.round(getRangeOverlapPercentile(hourStart, hourEnd, prevChange.dateTime, change.dateTime));
                    hourStat[i] += rangeOverlap;
                }
            }

            prevChange = change;
        }

        return hourStat;
    }

    private static double getRangeOverlapPercentile(LocalDateTime rangeStart, LocalDateTime rangeEnd, LocalDateTime usageStart, LocalDateTime usageEnd) {
        Validate.isTrue(rangeStart.isBefore(rangeEnd), "Range start is not before Range end.");
        Validate.isTrue(usageStart.isBefore(usageEnd), "Usage Start is not before usage end.");

        if (usageEnd.isBefore(rangeStart) || usageEnd.isEqual(rangeStart) || usageStart.isEqual(rangeEnd) || usageStart.isAfter(rangeEnd)) {
            //Ranges does not overlap
            return 0;
        }

        long rangeMsLength = ChronoUnit.MILLIS.between(rangeStart, rangeEnd);

        if (usageStart.isBefore(rangeStart)) {
            if (usageEnd.isAfter(rangeEnd)) {
                //Usage uses more than the range. 100 %
                return 100;
            } else {
                //Usage does not use whole range. Compute the percentile
                long usageInRangeMsLength = ChronoUnit.MILLIS.between(rangeStart, usageEnd);
                return ((double)usageInRangeMsLength * 100 / rangeMsLength);
            }
        } else {
            if (usageEnd.isAfter(rangeEnd)) {
                //Usage is larger than the range end. Needs alignment
                long usageInRangeMsLength = ChronoUnit.MILLIS.between(usageStart, rangeEnd);
                return ((double)usageInRangeMsLength * 100 / rangeMsLength);
            } else {
                long usageInRangeMsLength = ChronoUnit.MILLIS.between(usageStart, usageEnd);
                return ((double)usageInRangeMsLength * 100 / rangeMsLength);
            }
        }
    }

}
