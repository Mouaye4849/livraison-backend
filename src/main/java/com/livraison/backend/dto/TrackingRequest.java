package com.livraison.backend.dto;

import com.livraison.backend.entity.TrackingStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class TrackingRequest {

    private UUID colisId;

    private TrackingStatus status;

    private String message;

    private String locationName;

}
