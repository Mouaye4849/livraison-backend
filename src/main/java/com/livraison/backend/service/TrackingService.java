package com.livraison.backend.service;

import com.livraison.backend.dto.TrackingEventDTO;
import com.livraison.backend.entity.Colis;
import com.livraison.backend.entity.TrackingStatus;

import java.util.List;
import java.util.UUID;

public interface TrackingService {
    void createEvent(Colis colis, TrackingStatus status, String message, String locationName);
    List<TrackingEventDTO> getEventsByColisId(UUID colisId);
}
