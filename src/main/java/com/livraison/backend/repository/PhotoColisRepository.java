package com.livraison.backend.repository;

import com.livraison.backend.entity.PhotoColis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PhotoColisRepository extends JpaRepository<PhotoColis, UUID> {

    List<PhotoColis> findByColisId(UUID colisId);

}