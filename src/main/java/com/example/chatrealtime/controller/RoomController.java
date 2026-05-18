package com.example.chatrealtime.controller;

import com.example.chatrealtime.entities.Message;
import com.example.chatrealtime.entities.Room;
import com.example.chatrealtime.repositry.MessageRepository;
import com.example.chatrealtime.repositry.RoomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/rooms")
public class RoomController {

    private final RoomRepository roomRepository;
    private final MessageRepository messageRepository;

    public RoomController(RoomRepository roomRepository,
                          MessageRepository messageRepository) {
        this.roomRepository = roomRepository;
        this.messageRepository = messageRepository;
    }

    // CREATE ROOM
    @PostMapping
    public ResponseEntity<?> createRoom(@RequestParam String roomId) {

        if (roomRepository.findByRoomId(roomId) != null) {
            return ResponseEntity.badRequest().body("Room already exist");
        }

        Room room = new Room();
        room.setRoomId(roomId);

        Room savedRoom = roomRepository.save(room);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedRoom);
    }

    // GET ROOM
    @GetMapping("/{roomId}")
    public ResponseEntity<?> joinRoom(@PathVariable String roomId) {

        Room room = roomRepository.findByRoomId(roomId);

        if (room == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(room);
    }

    // GET MESSAGES (PAGINATION)
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<Message>> getMessages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        Room room = roomRepository.findByRoomId(roomId);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Message> messages =
                messageRepository.findByRoomIdOrderByTimeStampDesc(roomId, pageable);

        return ResponseEntity.ok(messages.getContent());
    }
}