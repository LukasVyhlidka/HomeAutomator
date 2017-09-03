package org.vyhlidka.homeautomation.repo;

import org.vyhlidka.homeautomation.domain.Room;

import java.util.List;

public interface RoomRepository {

    List<Room> getRooms();

    Room getRoom(int id);

    Room saveRoom(Room room);

    void deleteRoom(int id);

}
