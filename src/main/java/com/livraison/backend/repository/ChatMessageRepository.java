package com.livraison.backend.repository;

import com.livraison.backend.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findByColisIdOrderByCreatedAtAsc(UUID colisId);
}
