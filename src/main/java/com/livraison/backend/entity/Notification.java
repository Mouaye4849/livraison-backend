package com.livraison.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    private TypeNotification type;

    private String message;

    @Enumerated(EnumType.STRING)
    private StatutNotification statut;


    private LocalDateTime dateEnvoi;
    private String role;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User destinataire;
}
