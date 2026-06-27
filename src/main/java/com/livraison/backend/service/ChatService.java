package com.livraison.backend.service;

import com.livraison.backend.entity.ChatMessage;

import java.util.List;
import java.util.UUID;

public interface ChatService {

    ChatMessage save(ChatMessage msg);

    List<ChatMessage> getMessages(UUID colisId);
}
