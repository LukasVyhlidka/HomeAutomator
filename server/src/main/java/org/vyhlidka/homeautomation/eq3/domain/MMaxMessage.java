package org.vyhlidka.homeautomation.eq3.domain;

import java.util.Collections;
import java.util.List;

public class MMaxMessage extends MaxMessage {

    public final int index;

    public final int count;

    public final List<MaxRoomMeta> rooms;

    public final List<MaxDeviceMeta> devices;

    public MMaxMessage(final String message, int index, int count, List<MaxRoomMeta> rooms, List<MaxDeviceMeta> devices) {
        super("M", message);

        this.index = index;
        this.count = count;
        this.rooms = Collections.unmodifiableList(rooms);
        this.devices = Collections.unmodifiableList(devices);
    }

    @Override
    public String getMessageType() {
        return "M";
    }

    public static class MaxRoomMeta {

        public final int id;

        public final String name;

        public final int rfAddress;

        public MaxRoomMeta(final int id, final String name, final int rfAddress) {
            this.id = id;
            this.name = name;
            this.rfAddress = rfAddress;
        }
    }

    public static class MaxDeviceMeta {

        public final DeviceTypeMeta type;

        public final int rfAddress;

        public final String serialNumber;

        public final String name;

        public final int roomId;

        public MaxDeviceMeta(final DeviceTypeMeta type, final int rfAddress, final String serialNumber, final String name, final int roomId) {
            this.type = type;
            this.rfAddress = rfAddress;
            this.serialNumber = serialNumber;
            this.name = name;
            this.roomId = roomId;
        }
    }

    public enum DeviceTypeMeta {

        Cube(0),

        HeatingThermostat(1),

        HeatingThermostatPlus(2),

        WallMountedThermostat(3),

        ShutterContact(4),

        PushButton(5);

        public static DeviceTypeMeta getByNumber(int number) {
            for (DeviceTypeMeta type : values()) {
                if (type.typeNumber == number) {
                    return type;
                }
            }
            return null;
        }

        public final int typeNumber;

        DeviceTypeMeta(final int typeNumber) {
            this.typeNumber = typeNumber;
        }
    }
}
