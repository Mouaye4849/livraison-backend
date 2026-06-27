package com.livraison.backend.repository;

import com.livraison.backend.entity.Colis;
import com.livraison.backend.entity.StatutColis;
import com.livraison.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ColisRepository extends JpaRepository<Colis, UUID> {
    List<Colis> findByUser(User user);
    List<Colis> findByStatut(StatutColis statut);
    List<Colis> findByStatutAndVilleDepartAndVilleArrivee(
            StatutColis statut,
            String villeDepart,
            String villeArrivee
    );
}
