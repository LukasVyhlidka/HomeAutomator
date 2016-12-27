package org.vyhlidka.homeautomation.eq3.domain;

import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.List;

/**
 * Represents the L-type of the MAX message.
 */
public class LMaxMessage extends MaxMessage {

    public final List<MaxDevice> devices;

    public LMaxMessage(final String message, final List<MaxDevice> devices) {
        super(message);

        Validate.notNull(devices, "devices can not be null;");
        this.devices = Collections.unmodifiableList(devices);
    }

    public static class MaxDevice {

        public MaxDeviceType type;

        public final int rfAddress;

        public final byte unknown;

        public final int flags;

        public final byte valvePosition;

        /* temp * 2 (divide by two to get the temperature) */
        public final int temperature;

        public final int dateUntil;

        public final byte timeUntil;

        /* temp * 10 (divide by ten to get the actual temperature) */
        public final int actualTemperature;

        public MaxDevice(final MaxDeviceType type, final int rfAddress, final byte unknown, final int flags,
                         final byte valvePosition, final int temperature,
                         final int dateUntil, final byte timeUntil, final int actualTemperature) {
            this.type = type;
            this.rfAddress = rfAddress;
            this.unknown = unknown;
            this.flags = flags;
            this.valvePosition = valvePosition;
            this.temperature = temperature;
            this.dateUntil = dateUntil;
            this.timeUntil = timeUntil;
            this.actualTemperature = actualTemperature;
        }
    }

    public enum MaxDeviceType {
        ECO, VALVE, THERMOSTAT;
    }
}
