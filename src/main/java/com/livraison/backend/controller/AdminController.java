package com.livraison.backend.controller;

import com.livraison.backend.dto.UserResponseDTO;
import com.livraison.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public Page<UserResponseDTO> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminService.getUsers(page, size);
    }

    @GetMapping("/users/{id}")
    public UserResponseDTO getUser(@PathVariable UUID id) {
        return adminService.getUserById(id);
    }

    @PutMapping("/users/{id}")
    public UserResponseDTO updateUser(
            @PathVariable UUID id,
            @RequestBody UserResponseDTO dto
    ) {
        return adminService.updateUser(id, dto);
    }

    @PutMapping("/users/{id}/disable")
    public String disableUser(@PathVariable UUID id) {
        adminService.disableUser(id);
        return "User disabled";
    }

    @PutMapping("/users/{id}/enable")
    public String enableUser(@PathVariable UUID id) {
        adminService.enableUser(id);
        return "User enabled";
    }

    @PutMapping("/users/{id}/promote")
    public String promote(@PathVariable UUID id) {
        adminService.promoteToAdmin(id);
        return "Promoted to ADMIN";
    }

    @PutMapping("/users/{id}/demote")
    public String demote(@PathVariable UUID id) {
        adminService.demoteToUser(id);
        return "Demoted to USER";
    }

    @DeleteMapping("/users/{id}")
    public String delete(@PathVariable UUID id) {
        adminService.deleteUser(id);
        return "User deleted";
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {

        long totalColis = adminService.countColis();
        long totalTrajets = adminService.countTrajets();

        return Map.of(
                "totalColis", totalColis,
                "totalTrajets", totalTrajets,
                "croissance", 0,
                "colisParJour", List.of(5, 8, 3, 6, 10, 4, 7)
        );
    }
}