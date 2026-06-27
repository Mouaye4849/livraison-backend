package com.livraison.backend.service;

import com.livraison.backend.auth.BankilyAuthService;
import com.livraison.backend.dto.PaiementDTO;
import com.livraison.backend.dto.PaymentApiRequest;
import com.livraison.backend.dto.PaymentApiResponse;
import com.livraison.backend.entity.*;
import com.livraison.backend.repository.ColisRepository;
import com.livraison.backend.repository.PaiementRepository;
import com.livraison.backend.repository.UserRepository;
import com.livraison.backend.service.NotificationService;
import com.livraison.backend.service.PaiementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaiementServiceImpl implements PaiementService {

    private final PaiementRepository paiementRepository;
    private final ColisRepository colisRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private final WebClient webClient;
    private final BankilyAuthService bankilyAuthService;

    private static final double COMMISSION_RATE = 0.10;

    @Override
    public PaiementDTO payer(
            UUID colisId,
            TypePaiement typePaiement,
            String clientPhone,
            String passcode
    ) {


        Colis colis = colisRepository.findById(colisId)
                .orElseThrow(() ->
                        new RuntimeException("Colis introuvable ❌"));

        if (colis.getStatut() != StatutColis.LIVRE) {
            throw new RuntimeException(
                    "Le colis doit être livré avant le paiement ❌"
            );
        }

        if (colis.getTrajet() == null) {
            throw new RuntimeException(
                    "Ce colis n'est pas associé à un trajet ❌"
            );
        }

        if (colis.getPrixProposeMRU() == null
                || colis.getPrixProposeMRU() <= 0) {

            throw new RuntimeException(
                    "Le prix du colis est invalide ❌"
            );
        }

        if (paiementRepository.existsByColis(colis)) {
            throw new RuntimeException(
                    "Ce colis est déjà payé ❌"
            );
        }

        // =========================
        // Utilisateur connecté
        // =========================

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User payeur = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Utilisateur introuvable ❌"
                        ));

        User voyageur = colis.getTrajet().getVoyageur();

        // =========================
        // Calcul montant
        // =========================

        double montant = colis.getPrixProposeMRU();

        double commissionAdmin =
                montant * COMMISSION_RATE;

        double montantVoyageur =
                montant - commissionAdmin;

        // =========================
        // Authentification BANKILY
        // =========================

        String token =
                bankilyAuthService.getAccessToken();

        // =========================
        // Operation ID
        // =========================

        String operationId =
                UUID.randomUUID().toString();

        // =========================
        // Création requête BANKILY
        // =========================

        PaymentApiRequest apiRequest =
                new PaymentApiRequest();

        apiRequest.setClientPhone(clientPhone);

        apiRequest.setPasscode(passcode);

        apiRequest.setOperationId(operationId);

        apiRequest.setAmount(
                String.valueOf(montant)
        );

        apiRequest.setLanguage("FR");

        // =========================
        // Appel API BANKILY
        // =========================

        PaymentApiResponse apiResponse =
                webClient.post()
                        .uri("https://ebankily-tst.appspot.com/payment")

                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )

                        .contentType(MediaType.APPLICATION_JSON)

                        .bodyValue(apiRequest)

                        .retrieve()

                        .bodyToMono(
                                PaymentApiResponse.class
                        )

                        .block();

        // =========================
        // Vérification résultat
        // =========================

        StatutPaiement statut =
                apiResponse.getErrorCode().equals("0")
                        ? StatutPaiement.SUCCES
                        : StatutPaiement.ECHEC;

        // =========================
        // Création paiement
        // =========================

        Paiement paiement = Paiement.builder()

                .montantMRU(montant)

                .commissionAdmin(commissionAdmin)

                .montantVoyageur(montantVoyageur)

                .statut(statut)

                .typePaiement(typePaiement)

                .referenceTransaction(
                        apiResponse.getTransactionId()
                )

                .operationId(operationId)

                .errorCode(
                        apiResponse.getErrorCode()
                )

                .errorMessage(
                        apiResponse.getErrorMessage()
                )

                .dateConfirmation(LocalDateTime.now())

                .colis(colis)

                .voyageur(voyageur)

                .payeur(payeur)

                .build();

        paiementRepository.save(paiement);

        // =========================
        // Si paiement succès
        // =========================

        if (statut == StatutPaiement.SUCCES) {

            colis.setStatut(StatutColis.TERMINE);

            colisRepository.save(colis);

            // =========================
            // Notifications
            // =========================

            notificationService.createNotification(
                    payeur,
                    "Paiement effectué avec succès ✅",
                    TypeNotification.SYSTEME
            );

            notificationService.createNotification(
                    voyageur,
                    "Vous avez reçu "
                            + montantVoyageur
                            + " MRU 💰",
                    TypeNotification.SYSTEME
            );
        }

        // =========================
        // Retour DTO
        // =========================

        return PaiementDTO.builder()

                .id(paiement.getId())

                .montantMRU(
                        paiement.getMontantMRU()
                )

                .commissionAdmin(
                        paiement.getCommissionAdmin()
                )

                .montantVoyageur(
                        paiement.getMontantVoyageur()
                )

                .statut(
                        paiement.getStatut()
                )

                .typePaiement(
                        paiement.getTypePaiement()
                )

                .referenceTransaction(
                        paiement.getReferenceTransaction()
                )

                .dateConfirmation(
                        paiement.getDateConfirmation()
                )

                .build();
    }

    @Override
    public List<PaiementDTO> getMyPayments() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Utilisateur introuvable"
                        ));

        return paiementRepository.findByPayeur(user)

                .stream()

                .map(p -> PaiementDTO.builder()

                        .id(p.getId())

                        .montantMRU(
                                p.getMontantMRU()
                        )

                        .statut(
                                p.getStatut()
                        )

                        .typePaiement(
                                p.getTypePaiement()
                        )

                        .referenceTransaction(
                                p.getReferenceTransaction()
                        )

                        .dateConfirmation(
                                p.getDateConfirmation()
                        )

                        .build())

                .toList();
    }

    @Override
    public Double getAdminRevenue() {

        return paiementRepository.findAll()

                .stream()

                .mapToDouble(
                        Paiement::getCommissionAdmin
                )

                .sum();
    }

    @Override
    public long getTotalTransactions() {

        return paiementRepository.count();
    }
}