package org.vyhlidka.homeautomation.endpoint;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.vyhlidka.homeautomation.domain.Room;
import org.vyhlidka.homeautomation.repo.RoomRepository;

import java.util.List;

/**
 * Created by lucky on 18.12.16.
 */
@RestController
@RequestMapping(value = "/rooms", produces = MediaType.APPLICATION_JSON_VALUE)
public class RoomEndpoint {

    private final RoomRepository roomRepository;

    @Autowired
    public RoomEndpoint(final RoomRepository roomRepository) {
        Validate.notNull(roomRepository, "roomRepository can not be null;");
        this.roomRepository = roomRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<Room> getBoilers() {
        return this.roomRepository.getRooms();
    }

    @RequestMapping("/{id}")
    public ResponseEntity<Room> getRoom(@PathVariable(name = "id") Integer id) {
        if (id == null) {
            return ResponseEntity.badRequest().body(null);
        }

        Room r = this.roomRepository.getRoom(id);
        return ResponseEntity.ok(r);
    }

}
