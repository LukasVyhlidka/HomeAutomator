package org.vyhlidka.homeautomation.repo;

import org.springframework.stereotype.Repository;
import org.vyhlidka.homeautomation.domain.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class RoomInMemoryRepository implements RoomRepository {

    private final Map<Integer, Room> roomMap = new ConcurrentHashMap<>();

    @Override
    public List<Room> getRooms() {
        return new ArrayList<>(this.roomMap.values());
    }

    @Override
    public Room getRoom(final int id) {
        return this.roomMap.get(id);
    }

    @Override
    public Room saveRoom(final Room room) {
        this.roomMap.put(room.id, room);
        return room;
    }

    @Override
    public void deleteRoom(final int id) {
        this.roomMap.remove(id);
    }
}
