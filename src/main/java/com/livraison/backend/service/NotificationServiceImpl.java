package com.livraison.backend.service;

import com.livraison.backend.dto.NotificationDTO;
import com.livraison.backend.entity.*;
import com.livraison.backend.repository.NotificationRepository;
import com.livraison.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public List<NotificationDTO> getMyNotifications() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.findByDestinataireOrderByDateEnvoiDesc(user)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private NotificationDTO mapToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .statut(notification.getStatut())
                .type(notification.getType())
                .dateEnvoi(notification.getDateEnvoi())
                .build();
    }

    @Override
    public void sendNotification(String message) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = Notification.builder()
                .message(message)
                .statut(StatutNotification.ENVOYE)
                .destinataire(user)
                .dateEnvoi(LocalDateTime.now())
                .type(TypeNotification.SYSTEME)
                .build();

        notificationRepository.save(notification);
    }

    @Override
    public void markAsRead(UUID id) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getDestinataire().getEmail().equals(email)) {
            throw new RuntimeException("Access denied");
        }

        notification.setStatut(StatutNotification.LU);
        notificationRepository.save(notification);
    }

    @Override
    public long getUnreadCount() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.countByDestinataireAndStatut(
                user,
                StatutNotification.ENVOYE
        );
    }
    @Override
    public void notifyAdmin(String message, TypeNotification type) {

        List<User> admins = userRepository.findByRole(Role.ROLE_ADMIN);

        for (User admin : admins) {
            Notification n = Notification.builder()
                    .message(message)
                    .type(type)
                    .statut(StatutNotification.ENVOYE)
                    .dateEnvoi(LocalDateTime.now())
                    .destinataire(admin) // ✅ هذا هو الحل
                    .build();

            notificationRepository.save(n);
        }
    }

    @Override
    public void createNotification(User user, String message, TypeNotification type) {

        Notification notification = Notification.builder()
                .destinataire(user)
                .message(message)
                .type(type)
                .statut(StatutNotification.ENVOYE)
                .dateEnvoi(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }
}
