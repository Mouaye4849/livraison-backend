package com.livraison.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpsLocationDTO {

    private UUID id;
    private UUID colisId;
    private Double latitude;
    private Double longitude;

    // Serialize as ISO string "2025-06-15T10:30:00" — not as Jackson's default
    // integer array [2025,6,15,10,30,0] which breaks the mobile GpsLocation.createdAt string field.
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
