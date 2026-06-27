package com.livraison.backend.controller;

import com.livraison.backend.dto.TrajetResponseDTO;
import com.livraison.backend.service.TrajetService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/trajets")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTrajetController {

    private final TrajetService trajetService; // ✅ بدل AdminService

    // 📥 Pending
    @GetMapping("/pending")
    public List<TrajetResponseDTO> getPendingTrajets() {
        return trajetService.getPendingTrajets();
    }

    // ✅ Approve
    @PutMapping("/{id}/approve")
    public void approve(@PathVariable UUID id) {
        trajetService.approveTrajet(id);
    }

    // ❌ Reject
    @PutMapping("/{id}/reject")
    public void reject(@PathVariable UUID id) {
        trajetService.rejectTrajet(id);
    }
}
