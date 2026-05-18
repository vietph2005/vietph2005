package com.example.chatrealtime.controller;

import com.example.chatrealtime.entities.Message;
import com.example.chatrealtime.entities.Room;
import com.example.chatrealtime.playload.MessageRequest;
import com.example.chatrealtime.repositry.RoomRepository;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class ChatController {
    private RoomRepository roomRepository;

    public ChatController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }
    @MessageMapping("/sendMessage/{roomId}")
    @SendTo("/topic/room{roomId}")
    public Message sendMessage(@RequestBody MessageRequest request, @DestinationVariable String roomId){
        Room room = roomRepository.findByRoomId(roomId);
        Message message = new Message();
        message.setContent(request.getContent());
        message.setSender(request.getSender());
        message.setTimeStamp(request.getMessageTime());
        if(room != null){
            room.getMessages().add(message);
            roomRepository.save(room);
        }
        else{
            throw new RuntimeException("room not found");
        }
        return message;
    }
}
