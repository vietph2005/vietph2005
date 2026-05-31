package com.example.chatrealtime.controller;

import com.example.chatrealtime.entities.Message;
import com.example.chatrealtime.entities.Room;
import com.example.chatrealtime.playload.MessageRequest;
import com.example.chatrealtime.repositry.MessageRepository;
import com.example.chatrealtime.repositry.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatController {

    private final RoomRepository roomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageRepository messageRepository;

    public ChatController(RoomRepository roomRepository, SimpMessagingTemplate messagingTemplate) {
        this.roomRepository = roomRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // Chức năng gửi tin nhắn và thông báo hệ thống khi có người vào/ra phòng
    @MessageMapping("/sendMessage/{roomId}")
    public void sendMessage(@Payload MessageRequest request, @DestinationVariable String roomId) {
        Room room = roomRepository.findByRoomId(roomId);
        if (room == null) {
            Message errorMsg = new Message();
            errorMsg.setSender("Hệ thống");
            errorMsg.setContent("LỖI: Phòng không tồn tại hoặc đã bị đóng!");
            messagingTemplate.convertAndSend("/topic/room/" + roomId, errorMsg);
            return;
        }

        Message message = new Message();
        message.setRoomId(roomId);
        message.setContent(request.getContent());
        message.setSender(request.getSender());

        if (request.getMessageTime() != null) {
            String timeStr = request.getMessageTime().replace("Z", "");
            message.setTimeStamp(LocalDateTime.parse(timeStr));
        } else {
            message.setTimeStamp(LocalDateTime.now());
        }

        // Lưu tin nhắn vào DB công cụ
        messageRepository.save(message);

        // Phát sóng tin nhắn thời gian thực
        messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
    }
}
