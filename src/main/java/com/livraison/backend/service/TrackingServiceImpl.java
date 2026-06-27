package com.livraison.backend.service;

import com.livraison.backend.dto.TrackingEventDTO;
import com.livraison.backend.entity.Colis;
import com.livraison.backend.entity.TrackingEvent;
import com.livraison.backend.entity.TrackingStatus;
import com.livraison.backend.repository.TrackingEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrackingServiceImpl implements TrackingService {

    private final TrackingEventRepository trackingEventRepository;

    @Override
    public void createEvent(Colis colis, TrackingStatus status, String message, String locationName) {
        TrackingEvent event = TrackingEvent.builder()
                .colis(colis)
                .trackingStatus(status)
                .message(message)
                .locationName(locationName)
                .createdAt(LocalDateTime.now())
                .build();

        trackingEventRepository.save(event);

        System.out.println("[TRACKING] Event saved → status=" + status
                + " | colisId=" + colis.getId()
                + " | location=" + locationName);
    }

    @Override
    public List<TrackingEventDTO> getEventsByColisId(UUID colisId) {
        return trackingEventRepository.findByColisIdOrderByCreatedAtAsc(colisId)
                .stream()
                .map(e -> TrackingEventDTO.builder()
                        .status(e.getTrackingStatus().name())
                        .message(e.getMessage())
                        .locationName(e.getLocationName())
                        .createdAt(e.getCreatedAt())
                        .build()
                ).toList();
    }
}
