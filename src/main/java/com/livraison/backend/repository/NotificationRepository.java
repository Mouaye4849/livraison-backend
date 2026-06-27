package com.livraison.backend.repository;

import com.livraison.backend.entity.Notification;
import com.livraison.backend.entity.StatutNotification;
import com.livraison.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByDestinataire(User user);
    List<Notification> findByDestinataireOrderByDateEnvoiDesc(User user);
    long countByDestinataireAndStatut(User user, StatutNotification statut);
    List<Notification> findByRoleOrderByDateEnvoiDesc(String role);
}