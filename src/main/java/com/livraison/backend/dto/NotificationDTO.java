package com.livraison.backend.dto;

import com.livraison.backend.entity.StatutNotification;
import com.livraison.backend.entity.TypeNotification;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {

    private UUID id;

    private String message;

    private StatutNotification statut;

    private TypeNotification type;

    private LocalDateTime dateEnvoi;
}