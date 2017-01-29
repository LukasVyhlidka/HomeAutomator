package org.vyhlidka.homeautomation.eq3;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.vyhlidka.homeautomation.eq3.domain.MMaxMessage;

import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MMessageParser implements MessageParser<MMaxMessage> {

    private static final Logger logger = LoggerFactory.getLogger(LMessageParser.class);

    private static final Pattern msgPattern = Pattern.compile("^(\\w):(.+)$");
    private static final Pattern mMsgPattern = Pattern.compile("^(\\d{2}),(\\d{2}),([^,]+)$");

    @Override
    public String getMessageType() {
        return "M";
    }

    @Override
    public MMaxMessage parse(final String message) {
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

        Matcher dataMat = mMsgPattern.matcher(msg);
        if (! dataMat.matches()) {
            throw new IllegalArgumentException(MessageFormat.format("Message [{0}] is not valid eQ-3 M-type message", message));
        }

        int index = Integer.parseInt(dataMat.group(1), 16);
        int count = Integer.parseInt(dataMat.group(2), 16);
        byte[] data = Base64.getDecoder().decode(dataMat.group(3));
        int dataIndex = 2;

        // rooms
        List<MMaxMessage.MaxRoomMeta> rooms = new ArrayList<>();
        int roomCount = data[dataIndex++] & 0xff;
        for (int i = 0; i < roomCount; i++) {
            int roomId = data[dataIndex++] & 0xff;
            int roomNameLength = data[dataIndex++] & 0xff;
            String roomName = new String(data, dataIndex, roomNameLength);
            dataIndex += roomNameLength;
            int rfAddress = ByteBuffer.wrap(new byte[]{0, data[dataIndex++], data [dataIndex++], data[dataIndex++]}).getInt();

            rooms.add(new MMaxMessage.MaxRoomMeta(roomId, roomName, rfAddress));
        }

        //devices
        List<MMaxMessage.MaxDeviceMeta> devices = new ArrayList<>();
        int deviceCount = data[dataIndex++] & 0xff;
        for (int i = 0; i < deviceCount; i++) {
            MMaxMessage.DeviceTypeMeta deviceType = MMaxMessage.DeviceTypeMeta.getByNumber(data[dataIndex++] & 0xff);
            int rfAddress = ByteBuffer.wrap(new byte[]{0, data[dataIndex++], data [dataIndex++], data[dataIndex++]}).getInt();
            String serialNo = new String(data, dataIndex, 10);
            dataIndex += 10;

            int deviceNameLength = data[dataIndex++] & 0xff;
            String deviceName = new String(data, dataIndex, deviceNameLength);
            dataIndex += deviceNameLength;

            int roomId = data[dataIndex++] & 0xff;

            devices.add(new MMaxMessage.MaxDeviceMeta(deviceType, rfAddress, serialNo, deviceName, roomId));
        }

        return new MMaxMessage(message, index, count, rooms, devices);
    }
}
