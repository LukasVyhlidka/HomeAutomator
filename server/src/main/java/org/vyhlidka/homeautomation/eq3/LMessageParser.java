package org.vyhlidka.homeautomation.eq3;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;
import org.vyhlidka.homeautomation.eq3.domain.LMaxMessage;

import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LMessageParser implements MessageParser<LMaxMessage> {

    private static final Pattern msgPattern = Pattern.compile("^(\\w):(.+)$");

    @Override
    public String getMessageType() {
        return "L";
    }

    @Override
    public LMaxMessage parse(final String message) {
        Validate.notNull(message, "message can not be null;");

        Matcher mat = msgPattern.matcher(message);
        if (!mat.matches()) {
            throw new IllegalArgumentException(MessageFormat.format("Message [{0}] is not valid eQ-3 message", message));
        }

        String type = mat.group(1);
        String msg = mat.group(2);

        if (!this.getMessageType().equals(type)) {
            throw new IllegalArgumentException(MessageFormat.format("Message [{0}] is not of type [{1}]", message, this.getMessageType()));
        }

        byte[] data = Base64.getDecoder().decode(msg);

        //Validate that it ends to "ce 00"
        if (data.length < 2) {
            throw new IllegalArgumentException("Data part of the message has to be at least 2 bytes (0xce 0x00).");
        }

        if (data[data.length - 1] != 0x00 && data[data.length - 2] != 0xce) {
            throw new IllegalArgumentException("Message data does not end with 0xce 0x00 bytes");
        }

        List<LMaxMessage.MaxDevice> devices = new ArrayList<>();
        if (data.length > 2) {
            int i = 0;
            int subMessageLength = data[i];
            while (i < data.length && subMessageLength > 0) {
                if ((i + subMessageLength + 1) > data.length) {
                    throw new IllegalArgumentException("One SubMessage out of bounds exception.");
                }

                LMaxMessage.MaxDeviceType deviceType = LMaxMessage.MaxDeviceType.ECO;
                int rfAddress = ByteBuffer.wrap(new byte[]{0, data[i + 1], data [i + 2], data[i + 3]}).getInt();
                byte unknown = data[i + 4];
                int flags = ByteBuffer.wrap(new byte[]{0, 0, data[i + 5], data[i + 6]}).getInt();
                byte valvePosition = 0;
                int temperature = 0;
                int dateUntil = 0;
                byte timeUntil = 0;
                int actualTemperature = 0;

                if (subMessageLength > 6) {
                    deviceType = LMaxMessage.MaxDeviceType.VALVE;
                    valvePosition = data[i + 7];
                    temperature = (short)(data[i + 8] & 0x7F); // First bit belongs to actual temperature
                    dateUntil = ByteBuffer.wrap(new byte[]{0, 0, data[i + 9], data[i + 10]}).getInt();
                    timeUntil = data[i + 11];
                }

                if (subMessageLength > 11) {
                    deviceType = LMaxMessage.MaxDeviceType.THERMOSTAT;
                    actualTemperature = (short)(((((int)data[i + 8]) & 0x80) << 1) | (data[i + 12] & 0xFF)); // temperature first bit belongs to here
                }

                devices.add(new LMaxMessage.MaxDevice(
                        deviceType, rfAddress, unknown, flags, valvePosition,
                        temperature, dateUntil, timeUntil, actualTemperature));

                i = i + 1 + subMessageLength;
                if (i >= data.length) {
                    break;
                }

                subMessageLength = data[i];
            }
        }
        return new LMaxMessage(message, devices);
    }
}
