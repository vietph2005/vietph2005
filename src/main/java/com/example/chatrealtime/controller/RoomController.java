package com.example.chatrealtime.controller;

import com.example.chatrealtime.entities.Message;
import com.example.chatrealtime.entities.Room;
import com.example.chatrealtime.repositry.MessageRepository;
import com.example.chatrealtime.repositry.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // Dùng để gửi phản hồi WebSocket công khai

    // =================  XỬ LÝ WEBSOCKET (STOMP) =================

    // Xử lý khi nhận lệnh từ destination: /app/api/rooms/create-room (Phụ thuộc vào Prefix trong WebSocketConfig của bạn)
    @MessageMapping("/api/rooms/create-room")
    public void createRoomWS(@Payload Map<String, String> request) {
        String roomId = request.get("roomId");
        String sender = request.get("sender");

        // Chuẩn bị tin nhắn phản hồi hệ thống theo đúng định dạng Frontend chờ
        Message response = new Message();
        response.setSender("Hệ thống");
        response.setRoomId(roomId);

        if (roomId == null || roomId.trim().isEmpty()) {
            response.setContent("LỖI: Mã phòng không được để trống!");
            messagingTemplate.convertAndSend("/topic/room/" + roomId, response);
            return;
        }

        Room existingRoom = roomRepository.findByRoomId(roomId);
        if (existingRoom != null) {
            response.setContent("LỖI: Phòng này đã tồn tại!");
            messagingTemplate.convertAndSend("/topic/room/" + roomId, response);
            return;
        }

        // Lưu phòng mới vào DB
        Room newRoom = new Room();
        newRoom.setRoomId(roomId);
        roomRepository.save(newRoom);

        // Thành công -> Báo về client để chuyển màn
        response.setContent("Tạo phòng thành công!");
        messagingTemplate.convertAndSend("/topic/room/" + roomId, response);
    }

    // Xử lý khi nhận lệnh từ destination: /app/api/rooms/join-room
    @MessageMapping("/api/rooms/join-room")
    public void joinRoomWS(@Payload Map<String, String> request) {
        String roomId = request.get("roomId");

        Message response = new Message();
        response.setSender("Hệ thống");
        response.setRoomId(roomId);

        Room room = roomRepository.findByRoomId(roomId);
        if (room == null) {
            response.setContent("LỖI: Phòng không tồn tại!");
            messagingTemplate.convertAndSend("/topic/room/" + roomId, response);
            return;
        }

        // Thành công -> Báo về client để chuyển màn
        response.setContent("Vào phòng thành công!");
        messagingTemplate.convertAndSend("/topic/room/" + roomId, response);
    }


    // =================  XỬ LÝ HTTP REST API (GIỮ NGUYÊN) =================

    @GetMapping("/api/rooms/{roomId}/messages/page")
    public ResponseEntity<Page<Message>> getMessageHistoryPage(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messagePage = messageRepository.findByRoomIdOrderByTimeStampDesc(roomId, pageable);
        return ResponseEntity.ok(messagePage);
    }
}
