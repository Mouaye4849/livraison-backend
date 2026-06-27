package com.livraison.backend.service;

import com.livraison.backend.dto.GpsLocationDTO;
import com.livraison.backend.dto.GpsWsUpdateDTO;
import com.livraison.backend.entity.Colis;
import com.livraison.backend.entity.StatutColis;
import com.livraison.backend.entity.TrackingLocation;
import com.livraison.backend.repository.ColisRepository;
import com.livraison.backend.repository.TrackingLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class GpsWsService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ColisRepository colisRepository;
    private final TrackingLocationRepository trackingLocationRepository;

    private static final long DB_THROTTLE_MS = 2_000;
    private final ConcurrentHashMap<String, Long> lastDbWriteMs = new ConcurrentHashMap<>();

    /**
     * Processes a GPS update from a voyageur via STOMP /app/gps/update.
     *
     * Uses @Transactional(readOnly = false) to ensure colisRepository.findById()
     * always hits the database and never serves a stale Hibernate L1-cache entity
     * from a previous session.
     */
    @Transactional
    public void processUpdate(GpsWsUpdateDTO payload) {

        log.info("══════════════════════════════════════════════");
        log.info("[GPS-WS] ► Received STOMP update");
        log.info("[GPS-WS]   colisId (raw)  = '{}'", payload.getColisId());
        log.info("[GPS-WS]   latitude       = {}", payload.getLatitude());
        log.info("[GPS-WS]   longitude      = {}", payload.getLongitude());

        // ── 1. Validate payload ────────────────────────────────────────────────
        if (payload.getColisId() == null
                || payload.getLatitude() == null
                || payload.getLongitude() == null) {
            log.warn("[GPS-WS] ✗ DROPPED — null field in payload: colisId={} lat={} lon={}",
                    payload.getColisId(), payload.getLatitude(), payload.getLongitude());
            return;
        }

        // ── 2. Parse UUID ──────────────────────────────────────────────────────
        UUID colisId;
        try {
            colisId = UUID.fromString(payload.getColisId());
            log.info("[GPS-WS]   colisId (UUID) = {}", colisId);
        } catch (IllegalArgumentException e) {
            log.warn("[GPS-WS] ✗ DROPPED — colisId '{}' is not a valid UUID", payload.getColisId());
            return;
        }

        // ── 3. Load colis from DB (fresh read inside @Transactional) ──────────
        Colis colis = colisRepository.findById(colisId).orElse(null);
        if (colis == null) {
            log.warn("[GPS-WS] ✗ DROPPED — colis {} not found in database", colisId);
            return;
        }

        log.info("[GPS-WS]   colis.nom     = '{}'", colis.getNom());
        log.info("[GPS-WS]   colis.statut  = {} ({})",
                colis.getStatut(),
                colis.getStatut() == StatutColis.EN_COURS ? "✓ OK" : "✗ NOT EN_COURS — will be dropped");

        // ── 4. Statut guard — must be EN_COURS ────────────────────────────────
        if (colis.getStatut() != StatutColis.EN_COURS) {
            log.warn("══════════════════════════════════════════════");
            log.warn("[GPS-WS] ✗✗✗ GPS UPDATE DROPPED ✗✗✗");
            log.warn("[GPS-WS]   colisId      = {}", colisId);
            log.warn("[GPS-WS]   colis.nom    = '{}'", colis.getNom());
            log.warn("[GPS-WS]   statut actuel = {} (requis: EN_COURS)", colis.getStatut());
            log.warn("[GPS-WS]   topic qui aurait reçu = /topic/gps/{}", colisId);
            log.warn("[GPS-WS]   lat={} lon={} NON diffusés au client", payload.getLatitude(), payload.getLongitude());
            log.warn("[GPS-WS]   → SOLUTION: appeler PUT /api/colis/{}/start", colisId);
            log.warn("══════════════════════════════════════════════");
            return;
        }

        // ── 5. Broadcast via STOMP ────────────────────────────────────────────
        LocalDateTime now = LocalDateTime.now();

        GpsLocationDTO dto = GpsLocationDTO.builder()
                .colisId(colisId)
                .latitude(payload.getLatitude())
                .longitude(payload.getLongitude())
                .createdAt(now)
                .build();

        String topic = "/topic/gps/" + colisId;
        log.info("[COLISID] STEP 3/5 backend   broadcast topic   = '{}'", topic);
        log.info("[GPS-WS] ✓ Broadcasting to topic: {}", topic);
        log.info("[GPS-WS]   payload: lat={} lon={} at={}", payload.getLatitude(), payload.getLongitude(), now);

        try {
            messagingTemplate.convertAndSend(topic, dto);
            log.info("[GPS-WS] ✓ Broadcast sent successfully to {}", topic);
        } catch (Exception e) {
            log.error("[GPS-WS] ✗ Broadcast FAILED to {} — {}: {}", topic, e.getClass().getSimpleName(), e.getMessage(), e);
        }

        // ── 6. Rate-limited DB write ──────────────────────────────────────────
        long nowMs = System.currentTimeMillis();
        String colisIdStr = payload.getColisId();
        Long lastWrite = lastDbWriteMs.get(colisIdStr);

        if (lastWrite == null || nowMs - lastWrite >= DB_THROTTLE_MS) {
            try {
                TrackingLocation loc = TrackingLocation.builder()
                        .colis(colis)
                        .latitude(payload.getLatitude())
                        .longitude(payload.getLongitude())
                        .createdAt(now)
                        .build();
                // saveAndFlush forces the INSERT SQL to execute immediately (inside the
                // transaction, inside this try-catch) rather than deferring to commit time.
                // This ensures any DB error is caught here, not silently at commit.
                trackingLocationRepository.saveAndFlush(loc);
                lastDbWriteMs.put(colisIdStr, nowMs);
                log.info("[GPS-WS] ✓ DB write: saved TrackingLocation for colis {}", colisId);
            } catch (Exception e) {
                log.error("[GPS-WS] ✗ DB write FAILED for colis {} — {}: {}",
                        colisId, e.getClass().getSimpleName(), e.getMessage(), e);
            }
        } else {
            log.debug("[GPS-WS] DB write skipped (throttle: {}ms until next write)",
                    DB_THROTTLE_MS - (nowMs - lastWrite));
        }

        log.info("══════════════════════════════════════════════");
    }
}
