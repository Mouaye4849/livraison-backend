package com.livraison.backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingEventDTO {
    private String status;
    private String message;
    private String locationName;
    private LocalDateTime createdAt;
}
