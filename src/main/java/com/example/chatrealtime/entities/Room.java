package com.example.chatrealtime.entities;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    private String id; // MongoDB unique identifier

    private String roomId;


}
