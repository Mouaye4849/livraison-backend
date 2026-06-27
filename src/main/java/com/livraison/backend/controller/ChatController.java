package com.livraison.backend.controller;

import com.livraison.backend.entity.ChatMessage;
import com.livraison.backend.entity.Colis;
import com.livraison.backend.entity.User;
import com.livraison.backend.repository.ChatMessageRepository;
import com.livraison.backend.repository.ColisRepository;
import com.livraison.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin
public class ChatController {

    private final ChatMessageRepository chatRepository;
    private final UserRepository userRepository;
    private final ColisRepository colisRepository;

    /* =========================
       📥 GET MESSAGES
    ========================== */
    @GetMapping("/{colisId}")
    public List<ChatMessage> getMessages(@PathVariable UUID colisId) {

        return chatRepository.findByColisIdOrderByCreatedAtAsc(colisId);
    }


    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            @RequestBody ChatMessage request,
            Authentication auth
    ) {

        String email = auth.getName();

        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Colis colis = colisRepository.findById(request.getColisId())
                .orElseThrow(() -> new RuntimeException("Colis not found"));


        ChatMessage msg = new ChatMessage();
        msg.setColisId(request.getColisId());                                 
        msg.setSenderEmail(sender.getEmail());
        msg.setContent(request.getContent());
        msg.setType(request.getType());
        msg.setFileUrl(request.getFileUrl());
        msg.setCreatedAt(LocalDateTime.now());

        chatRepository.save(msg);

        return ResponseEntity.ok(msg);
    }


    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file
    ) {
        log.info("[UPLOAD] ► file={} size={} bytes contentType={}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadDir = Paths.get("uploads");
            Files.createDirectories(uploadDir);
            Path path = uploadDir.resolve(filename);
            Files.write(path, file.getBytes());

            String fileUrl = "http://localhost:8080/uploads/" + filename;
            log.info("[UPLOAD] ✓ saved → {} ({}B)", path.toAbsolutePath(), file.getSize());
            return ResponseEntity.ok(fileUrl);

        } catch (Exception e) {
            log.error("[UPLOAD] ✗ {} : {}", e.getClass().getSimpleName(), e.getMessage(), e);
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }
}