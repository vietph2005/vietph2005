package com.example.chatrealtime.repositry;

import com.example.chatrealtime.entities.Message;
import com.example.chatrealtime.entities.Room;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.awt.print.Pageable;
import java.util.List;

public interface RoomRepository extends MongoRepository<Room, String> {

    // get room using room id
    Room findByRoomId(String roomId);
}
