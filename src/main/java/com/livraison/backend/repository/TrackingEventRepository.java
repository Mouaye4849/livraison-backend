package com.livraison.backend.repository;

import com.livraison.backend.entity.Colis;
import com.livraison.backend.entity.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {

    List<TrackingEvent> findByColisIdOrderByCreatedAtAsc(UUID colisId);
}