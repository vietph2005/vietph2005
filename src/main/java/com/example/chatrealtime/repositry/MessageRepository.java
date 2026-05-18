package com.example.chatrealtime.repositry;

import com.example.chatrealtime.entities.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message, String> {

    Page<Message> findByRoomIdOrderByTimeStampDesc(
            String roomId,
            Pageable pageable
    );
}