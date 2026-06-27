package com.livraison.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GpsWsUpdateDTO {
    private String colisId;
    private Double latitude;
    private Double longitude;
}
