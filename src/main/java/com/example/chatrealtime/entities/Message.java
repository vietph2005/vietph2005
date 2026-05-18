package com.example.chatrealtime.entities;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Message {
    private String roomId;
    private String sender;

    private String content;

    private LocalDateTime timeStamp;

    public Message(String roomId,String sender, String content) {
        this.roomId = roomId;
        this.sender = sender;
        this.content = content;
        this.timeStamp = LocalDateTime.now();
    }
}
