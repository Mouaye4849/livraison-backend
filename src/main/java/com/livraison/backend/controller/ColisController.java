package com.livraison.backend.controller;

import com.livraison.backend.dto.ColisRequestDTO;
import com.livraison.backend.dto.ColisResponseDTO;
import com.livraison.backend.service.ColisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/colis")
@RequiredArgsConstructor
public class ColisController {

    private final ColisService colisService;


    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<ColisResponseDTO> create(
            @Valid @RequestBody ColisRequestDTO dto) {

        return ResponseEntity.ok(colisService.createColis(dto));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<ColisResponseDTO>> getAll() {

        return ResponseEntity.ok(colisService.getAllColis());
    }


    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ColisResponseDTO> getById(@PathVariable UUID id) {

        return ResponseEntity.ok(colisService.getColisById(id));
    }


    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{id}")
    public ResponseEntity<ColisResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody ColisRequestDTO dto) {

        return ResponseEntity.ok(colisService.updateColis(id, dto));
    }


    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{id}/publish")
    public ResponseEntity<ColisResponseDTO> publish(@PathVariable UUID id) {

        return ResponseEntity.ok(colisService.publishColis(id));
    }


    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ColisResponseDTO> cancel(@PathVariable UUID id) {

        return ResponseEntity.ok(colisService.cancelColis(id));
    }


    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ColisResponseDTO> delete(@PathVariable UUID id) {

        return ResponseEntity.ok(colisService.deleteColis(id));
    }


    @PreAuthorize("hasAnyRole('USER','VOYAGEUR')")
    @GetMapping("/me")
    public ResponseEntity<List<ColisResponseDTO>> getMyColis(Authentication auth) {

        return ResponseEntity.ok(
                colisService.getMyColis(auth.getName())
        );
    }

    
    @PreAuthorize("hasAnyRole('ADMIN', 'VOYAGEUR')")
    @GetMapping("/public")
    public ResponseEntity<List<ColisResponseDTO>> getPublic() {

        return ResponseEntity.ok(colisService.getPublicColis());
    }

    @PreAuthorize("hasRole('VOYAGEUR')")
    @GetMapping("/available/{trajetId}")
    public ResponseEntity<List<ColisResponseDTO>> getAvailableColis(
            @PathVariable UUID trajetId) {

        return ResponseEntity.ok(
                colisService.getAvailableColisForVoyageur(trajetId)
        );
    }

    @PreAuthorize("hasRole('VOYAGEUR')")
    @PutMapping("/{colisId}/assign/{trajetId}")
    public ResponseEntity<ColisResponseDTO> assign(
            @PathVariable UUID colisId,
            @PathVariable UUID trajetId) {

        return ResponseEntity.ok(
                colisService.assignTrajet(colisId, trajetId)
        );
    }


    @PreAuthorize("hasAnyRole('VOYAGEUR','ADMIN')")
    @PutMapping("/{id}/start")
    public ResponseEntity<ColisResponseDTO> start(@PathVariable UUID id) {

        return ResponseEntity.ok(colisService.startColis(id));
    }


    @PreAuthorize("hasAnyRole('VOYAGEUR','ADMIN')")
    @PutMapping("/{id}/finish")
    public ResponseEntity<ColisResponseDTO> finish(@PathVariable UUID id) {

        return ResponseEntity.ok(colisService.finishColis(id));
    }
}