package com.livraison.backend.repository;

import com.livraison.backend.entity.Colis;
import com.livraison.backend.entity.Paiement;
import com.livraison.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaiementRepository
        extends JpaRepository<Paiement, UUID> {

    // Vérifier double paiement
    boolean existsByColis(Colis colis);

    // Paiements du payeur
    List<Paiement> findByPayeur(User payeur);

    // Recherche par operationId
    Optional<Paiement> findByOperationId(String operationId);

    // Recherche par transaction BANKILY
    Optional<Paiement> findByReferenceTransaction(
            String referenceTransaction
    );
}
