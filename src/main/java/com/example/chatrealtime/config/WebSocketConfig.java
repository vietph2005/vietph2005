package com.example.chatrealtime.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Bật tính năng Message Broker cho WebSocket
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Cổng kết nối (Endpoint) ban đầu để Client gửi yêu cầu "bắt tay" (Handshake)
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*") // Cho phép tất cả các bên (Frontend/Postman) kết nối vào
                .withSockJS(); // Hỗ trợ SockJS dự phòng nếu trình duyệt cũ không hỗ trợ WebSocket thuần
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 1. Định tuyến gửi tin: Client gửi tin LÊN Server qua các đường dẫn bắt đầu bằng /app
        registry.setApplicationDestinationPrefixes("/app");

        // 2. Định tuyến nhận tin: Server đẩy tin XUỐNG Client qua các đường dẫn bắt đầu bằng /topic hoặc /queue
        // - /topic: Thường dùng cho chat phòng (Broadcast - nhiều người nhận)
        // - /queue: Thường dùng cho chat riêng tư 1-1 (Private - 1 người nhận)
        registry.enableSimpleBroker("/topic", "/queue");
    }
}
