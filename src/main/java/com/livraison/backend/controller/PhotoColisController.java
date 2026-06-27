package com.livraison.backend.controller;

import com.livraison.backend.dto.PhotoColisDTO;
import com.livraison.backend.service.PhotoColisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
public class PhotoColisController {

    private final PhotoColisService photoColisService;

    @PreAuthorize("hasAnyRole('USER','VOYAGEUR')")
    @PostMapping("/{colisId}")
    public ResponseEntity<PhotoColisDTO> upload(
            @PathVariable UUID colisId,
            @RequestParam("file") MultipartFile file) {

        return ResponseEntity.ok(photoColisService.uploadPhoto(colisId, file));
    }

    @PreAuthorize("hasAnyRole('USER','VOYAGEUR')")
    @GetMapping("/colis/{colisId}")
    public ResponseEntity<List<PhotoColisDTO>> getPhotos(
            @PathVariable UUID colisId) {

        return ResponseEntity.ok(photoColisService.getPhotosByColis(colisId));
    }

    @PreAuthorize("hasAnyRole('USER','VOYAGEUR')")
    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> delete(@PathVariable UUID photoId) {

        photoColisService.deletePhoto(photoId);

        return ResponseEntity.noContent().build();
    }
}