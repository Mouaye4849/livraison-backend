package com.livraison.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID colisId;

    private String senderEmail;

    private String content;

    private String type; // TEXT | IMAGE | AUDIO

    private String fileUrl;

    private LocalDateTime createdAt;
}