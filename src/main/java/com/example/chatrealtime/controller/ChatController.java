package com.example.chatrealtime.controller;

import com.example.chatrealtime.entities.Message;
import com.example.chatrealtime.entities.Room;
import com.example.chatrealtime.playload.MessageRequest;
import com.example.chatrealtime.repositry.RoomRepository;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.ArrayList;

@Controller
public class ChatController {

    private final RoomRepository roomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(RoomRepository roomRepository, SimpMessagingTemplate messagingTemplate) {
        this.roomRepository = roomRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/sendMessage/{roomId}")
    public void sendMessage(@Payload MessageRequest request, @DestinationVariable String roomId) {
        Room room = roomRepository.findByRoomId(roomId);
        if (room == null) {
            throw new RuntimeException("Room not found with ID: " + roomId);
        }

        Message message = new Message();
        message.setRoomId(roomId);
        message.setContent(request.getContent());
        message.setSender(request.getSender());
        message.setTimeStamp(request.getMessageTime() != null ? request.getMessageTime() : java.time.LocalDateTime.now());

        room.getMessages().add(message);
        roomRepository.save(room);

        messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
    }

    // XỬ LÝ LẠI LUỒNG TẠO PHÒNG VÀ LƯU VÀO DATABASE
    @MessageMapping("/create-room")
    public void handleCreateRoom(@Payload Message messageRequest) {
        String roomId = messageRequest.getRoomId();
        Room existingRoom = roomRepository.findByRoomId(roomId);

        // Nếu phòng chưa tồn tại, tiến hành tạo mới phòng trong DB
        if (existingRoom == null) {
            Room newRoom = new Room();
            newRoom.setRoomId(roomId);
            newRoom.setMessages(new ArrayList<>()); // Khởi tạo danh sách tin nhắn rỗng
            roomRepository.save(newRoom);
        }

        Message systemResponse = new Message(
                roomId,
                "Hệ thống",
                messageRequest.getSender() + " đã tạo phòng thành công!"
        );
        messagingTemplate.convertAndSend("/topic/room/" + roomId, systemResponse);
    }

    // KIỂM TRA PHÒNG CÓ TỒN TẠI TRƯỚC KHI CHO THAM GIA
    @MessageMapping("/join-room")
    public void handleJoinRoom(@Payload Message messageRequest) {
        String roomId = messageRequest.getRoomId();
        Room room = roomRepository.findByRoomId(roomId);

        Message systemResponse;

        if (room == null) {
            // NẾU KHÔNG CÓ PHÒNG: Gửi tin nhắn chứa tiền tố lỗi về Client để chặn truy cập
            systemResponse = new Message(
                    roomId,
                    "Hệ thống",
                    "LỖI: Phòng không tồn tại! Vui lòng kiểm tra lại mã phòng."
            );
        } else {
            // NẾU CÓ PHÒNG HỢP LỆ: Cho phép truy cập bình thường
            systemResponse = new Message(
                    roomId,
                    "Hệ thống",
                    messageRequest.getSender() + " đã tham gia vào phòng!"
            );
        }

        messagingTemplate.convertAndSend("/topic/room/" + roomId, systemResponse);
    }
}