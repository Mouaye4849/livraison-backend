package com.livraison.backend.repository;

import com.livraison.backend.entity.Trajet;
import com.livraison.backend.entity.TrajetStatut;
import com.livraison.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TrajetRepository extends JpaRepository<Trajet, UUID> {

    List<Trajet> findByStatut(TrajetStatut statut);

    List<Trajet> findByVoyageur(User voyageur);
    boolean existsByVoyageur(User user);


}
