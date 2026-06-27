package com.livraison.backend.repository;

import com.livraison.backend.entity.EmailOtp;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, UUID> {

    Optional<EmailOtp> findTopByEmailOrderByCreatedAtDesc(String email);

    @Modifying
    @Transactional
    @Query("UPDATE EmailOtp o SET o.used = true WHERE o.email = :email AND o.used = false")
    void invalidateAllForEmail(@Param("email") String email);
}
