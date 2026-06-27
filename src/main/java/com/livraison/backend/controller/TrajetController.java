package com.livraison.backend.controller;

import com.livraison.backend.dto.TrajetRequestDTO;
import com.livraison.backend.dto.TrajetResponseDTO;
import com.livraison.backend.service.TrajetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trajets")
@RequiredArgsConstructor
public class TrajetController {

    private final TrajetService trajetService;


    @PreAuthorize("hasRole('VOYAGEUR')")
    @PostMapping
    public ResponseEntity<TrajetResponseDTO> create(
            @RequestBody TrajetRequestDTO dto) {

        return ResponseEntity.ok(trajetService.createTrajet(dto));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<TrajetResponseDTO>> getAll() {

        return ResponseEntity.ok(trajetService.getAllTrajets());
    }


    @PreAuthorize("hasAnyRole('USER','VOYAGEUR','ADMIN')")
    @GetMapping("/open")
    public ResponseEntity<List<TrajetResponseDTO>> getOpen() {

        return ResponseEntity.ok(trajetService.getOpenTrajets());
    }


    @PreAuthorize("hasAnyRole('USER','VOYAGEUR','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<TrajetResponseDTO> getById(@PathVariable UUID id) {

        return ResponseEntity.ok(trajetService.getTrajetById(id));
    }


    @PreAuthorize("hasRole('VOYAGEUR')")
    @GetMapping("/me")
    public ResponseEntity<List<TrajetResponseDTO>> getMyTrajets() {

        return ResponseEntity.ok(trajetService.getMyTrajets());
    }


    @PreAuthorize("hasRole('VOYAGEUR')")
    @PutMapping("/{id}")
    public ResponseEntity<TrajetResponseDTO> update(
            @PathVariable UUID id,
            @RequestBody TrajetRequestDTO dto) {

        return ResponseEntity.ok(trajetService.updateTrajet(id, dto));
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {

        trajetService.deleteTrajet(id);
        return ResponseEntity.noContent().build();
    }


    @PreAuthorize("hasRole('VOYAGEUR')")
    @PutMapping("/{trajetId}/accept/{colisId}")
    public ResponseEntity<Void> accept(
            @PathVariable UUID trajetId,
            @PathVariable UUID colisId) {

        trajetService.acceptColis(trajetId, colisId);
        return ResponseEntity.ok().build();
    }
}