package com.livraison.backend.service;

import com.livraison.backend.dto.NotificationDTO;
import com.livraison.backend.entity.TypeNotification;
import com.livraison.backend.entity.User;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    List<NotificationDTO> getMyNotifications();

    void sendNotification(String message);
    void markAsRead(UUID id);

    void createNotification(User user, String message, TypeNotification type);
    long getUnreadCount();
    void notifyAdmin(String message, TypeNotification type);
}