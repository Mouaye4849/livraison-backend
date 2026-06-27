package com.livraison.backend.service;

import com.livraison.backend.entity.ChatMessage;
import com.livraison.backend.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository repo;

    @Override
    public ChatMessage save(ChatMessage msg) {
        msg.setCreatedAt(LocalDateTime.now());
        return repo.save(msg);
    }

    @Override
    public List<ChatMessage> getMessages(UUID colisId) {
        return repo.findByColisIdOrderByCreatedAtAsc(colisId);
    }

}
