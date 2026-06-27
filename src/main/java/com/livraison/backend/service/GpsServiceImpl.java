package com.livraison.backend.service;

import com.livraison.backend.dto.GpsLocationDTO;
import com.livraison.backend.entity.*;
import com.livraison.backend.exception.BusinessException;
import com.livraison.backend.exception.ResourceNotFoundException;
import com.livraison.backend.repository.ColisRepository;
import com.livraison.backend.repository.TrackingLocationRepository;
import com.livraison.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GpsServiceImpl implements GpsService {

    private final TrackingLocationRepository trackingLocationRepository;
    private final ColisRepository colisRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    @Override
    public GpsLocationDTO updateLocation(UUID colisId, Double latitude, Double longitude) {

        User currentUser = getCurrentUser();

        Colis colis = colisRepository.findById(colisId)
                .orElseThrow(() -> new ResourceNotFoundException("Colis introuvable"));

        if (colis.getTrajet() == null) {
            throw new BusinessException("Ce colis n'est pas encore assigné à un trajet");
        }

        if (!colis.getTrajet().getVoyageur().getId().equals(currentUser.getId())) {
            throw new BusinessException("Accès refusé : vous n'êtes pas le voyageur assigné à ce colis");
        }

        if (colis.getStatut() != StatutColis.EN_COURS) {
            throw new BusinessException("Le suivi GPS n'est disponible que pour les colis en cours de livraison");
        }

        TrackingLocation location = TrackingLocation.builder()
                .colis(colis)
                .latitude(latitude)
                .longitude(longitude)
                .createdAt(LocalDateTime.now())
                .build();

        GpsLocationDTO dto = toDTO(trackingLocationRepository.save(location));

        // Broadcast live to any subscribed client — mirrors what GpsWsService does for
        // the STOMP path so REST-submitted positions also trigger real-time map updates.
        try {
            messagingTemplate.convertAndSend("/topic/gps/" + colisId, dto);
            log.info("[GPS-REST] ✓ STOMP broadcast → /topic/gps/{}", colisId);
        } catch (Exception e) {
            log.warn("[GPS-REST] ✗ STOMP broadcast failed for {}: {}", colisId, e.getMessage());
        }

        return dto;
    }

    @Transactional(readOnly = true)
    @Override
    public GpsLocationDTO getLatestLocation(UUID colisId) {

        User currentUser = getCurrentUser();
        Role role = currentUser.getRole();

        Colis colis = colisRepository.findById(colisId)
                .orElseThrow(() -> new ResourceNotFoundException("Colis introuvable"));

        // Access rules:
        // - ADMIN    : always allowed
        // - USER     : colis owner (the client who shipped it)
        // - VOYAGEUR : the voyageur assigned to the trajet carrying this colis
        boolean isAdmin    = role == Role.ROLE_ADMIN;
        boolean isOwner    = role == Role.ROLE_USER
                && colis.getUser() != null
                && colis.getUser().getId().equals(currentUser.getId());
        boolean isVoyageur = role == Role.ROLE_VOYAGEUR
                && colis.getTrajet() != null
                && colis.getTrajet().getVoyageur() != null
                && colis.getTrajet().getVoyageur().getId().equals(currentUser.getId());

        log.debug("[GPS-REST] getLatestLocation colisId={} user={} role={} isAdmin={} isOwner={} isVoyageur={}",
                colisId, currentUser.getEmail(), role, isAdmin, isOwner, isVoyageur);

        if (!isAdmin && !isOwner && !isVoyageur) {
            log.warn("[GPS-REST] Access denied — user={} role={} is not owner/voyageur/admin for colis {}",
                    currentUser.getEmail(), role, colisId);
            throw new BusinessException("Accès refusé");
        }

        return trackingLocationRepository.findTopByColisIdOrderByCreatedAtDesc(colisId)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune position GPS disponible pour ce colis"));
    }

    private GpsLocationDTO toDTO(TrackingLocation location) {
        return GpsLocationDTO.builder()
                .id(location.getId())
                .colisId(location.getColis().getId())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .createdAt(location.getCreatedAt())
                .build();
    }
}
