package com.livraison.backend.repository;

import com.livraison.backend.entity.TrackingLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TrackingLocationRepository extends JpaRepository<TrackingLocation, UUID> {

    Optional<TrackingLocation> findTopByColisIdOrderByCreatedAtDesc(UUID colisId);
}
