package com.livraison.backend.controller;

import com.livraison.backend.dto.TrackingEventDTO;
import com.livraison.backend.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/colis/{colisId}")
    public ResponseEntity<List<TrackingEventDTO>> getTracking(@PathVariable UUID colisId) {
        return ResponseEntity.ok(trackingService.getEventsByColisId(colisId));
    }
}
