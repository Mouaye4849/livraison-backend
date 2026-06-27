package com.livraison.backend.service;

import com.livraison.backend.entity.Colis;
import com.livraison.backend.entity.User;
import com.livraison.backend.repository.ColisRepository;
import com.livraison.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ColisSecurityService {

    private final ColisRepository colisRepository;
    private final UserRepository userRepository;

    public void checkAccess(String email, UUID colisId) {

        // 1. user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. colis
        Colis colis = colisRepository.findById(colisId)
                .orElseThrow(() -> new RuntimeException("Colis not found"));

        // 3. client (user lié au colis)
        UUID clientId = colis.getUser().getId();

        // 4. voyageur (via trajet)
        UUID voyageurId = null;
        if (colis.getTrajet() != null && colis.getTrajet().getVoyageur() != null) {
            voyageurId = colis.getTrajet().getVoyageur().getId();
        }

        // 5. check access
        boolean allowed =
                user.getId().equals(clientId) ||
                        (voyageurId != null && user.getId().equals(voyageurId));

        if (!allowed) {
            throw new RuntimeException("Access denied ❌");
        }
    }
}