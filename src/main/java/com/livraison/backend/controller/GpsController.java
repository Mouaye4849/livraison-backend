package com.livraison.backend.controller;

import com.livraison.backend.dto.GpsLocationDTO;
import com.livraison.backend.dto.GpsUpdateRequestDTO;
import com.livraison.backend.service.GpsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/gps")
@RequiredArgsConstructor
public class GpsController {

    private final GpsService gpsService;

    /**
     * REST fallback for voyageur to push GPS (legacy — STOMP is preferred).
     * Requires ROLE_VOYAGEUR.
     */
    @PreAuthorize("hasRole('VOYAGEUR')")
    @PostMapping("/update")
    public ResponseEntity<GpsLocationDTO> updateLocation(
            @Valid @RequestBody GpsUpdateRequestDTO request) {

        return ResponseEntity.ok(
                gpsService.updateLocation(
                        request.getColisId(),
                        request.getLatitude(),
                        request.getLongitude()
                )
        );
    }

    /**
     * Latest GPS position for a colis.
     * Allowed roles:
     *   - ROLE_USER      (colis owner / client — pressed "Suivre en direct")
     *   - ROLE_VOYAGEUR  (the assigned voyageur)
     *   - ROLE_ADMIN
     * Access control is enforced inside GpsServiceImpl.getLatestLocation().
     */
    @PreAuthorize("hasAnyRole('USER', 'VOYAGEUR', 'ADMIN')")
    @GetMapping("/latest/{colisId}")
    public ResponseEntity<GpsLocationDTO> getLatest(@PathVariable UUID colisId) {

        return ResponseEntity.ok(gpsService.getLatestLocation(colisId));
    }
}
