package com.livraison.backend.controller;

import com.livraison.backend.entity.Notification;
import com.livraison.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

    @RestController
    @RequestMapping("/api/admin")
    @RequiredArgsConstructor
    @PreAuthorize("hasRole('ADMIN')")
    public class AdminNotificationController {

        private final NotificationRepository notificationRepository;

        @GetMapping("/notifications")
        public List<Notification> getAdminNotifications() {
            return notificationRepository.findByRoleOrderByDateEnvoiDesc("ADMIN");
        }
    }
