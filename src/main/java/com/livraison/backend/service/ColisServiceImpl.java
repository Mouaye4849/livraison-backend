package com.livraison.backend.service;

import com.livraison.backend.dto.ColisRequestDTO;
import com.livraison.backend.dto.ColisResponseDTO;
import com.livraison.backend.entity.*;
import com.livraison.backend.exception.BusinessException;
import com.livraison.backend.exception.ResourceNotFoundException;
import com.livraison.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ColisServiceImpl implements ColisService {

    private final ColisRepository colisRepository;
    private final UserRepository userRepository;
    private final TrajetRepository trajetRepository;
    private final NotificationService notificationService;
    private final PaiementRepository paiementRepository;
    private final TrackingEventRepository trackingEventRepository;


    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }




    @Override
    public ColisResponseDTO createColis(ColisRequestDTO dto) {

        User user = getCurrentUser();

        Colis colis = mapToEntity(dto);
        colis.setUser(user);

        Colis saved = colisRepository.save(colis);
        createTracking(
                saved,
                TrackingStatus.COLIS_CREE,
                "Le colis a été créé",
                saved.getVilleDepart()
        );

        notificationService.createNotification(
                user,
                "Votre colis a été créé avec succès",
                TypeNotification.SYSTEME
        );

        notificationService.notifyAdmin("Nouveau colis cree", TypeNotification.SYSTEME);

        return mapToResponseDTO(saved);
    }

    @Override
    public void createTracking(
            Colis colis,
            TrackingStatus status,
            String message,
            String location
    ) {

        TrackingEvent event = new TrackingEvent();

        event.setColis(colis);
        event.setTrackingStatus(status);
        event.setMessage(message);
        event.setLocationName(location);

        trackingEventRepository.save(event);

        colis.setTrackingStatus(status);

        colisRepository.save(colis);
    }


    @Override
    public ColisResponseDTO assignTrajet(UUID colisId, UUID trajetId) {

        User currentUser = getCurrentUser();

        Colis colis = colisRepository.findById(colisId)
                .orElseThrow(() -> new ResourceNotFoundException("Colis introuvable"));

        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable"));


        if (!trajet.getVoyageur().getId().equals(currentUser.getId())) {
            throw new BusinessException("Accès refusé");
        }

        // ✅ checks
        if (colis.getStatut() != StatutColis.PUBLIE) {
            throw new BusinessException("Colis non disponible");
        }

        if (!colis.getVilleDepart().equals(trajet.getOrigine()) ||
                !colis.getVilleArrivee().equals(trajet.getDestination())) {
            throw new BusinessException("Trajet non compatible");
        }

        if (trajet.getCapaciteKg() < colis.getPoidsKg()) {
            throw new BusinessException("Capacité insuffisante");
        }

        User owner = userRepository.findById(colis.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User introuvable"));


        colis.setTrajet(trajet);
        colis.setStatut(StatutColis.ACCEPTE);

        createTracking(
                colis,
                TrackingStatus.COLIS_ACCEPTE,
                "Colis accepté par le voyageur",
                trajet.getOrigine()
        );

        trajet.setCapaciteKg(trajet.getCapaciteKg() - colis.getPoidsKg());

        if (trajet.getCapaciteKg() <= 0) {
            trajet.setStatut(TrajetStatut.COMPLET);
        }

        colisRepository.save(colis);
        trajetRepository.save(trajet);


        notificationService.createNotification(
                owner,
                "Votre colis a été accepté par un voyageur ",
                TypeNotification.SYSTEME
        );

        notificationService.notifyAdmin("Colis accepte par un voyageur", TypeNotification.SYSTEME);

        return mapToResponseDTO(colis);
    }

    @Override
    public List<ColisResponseDTO> getAllColis() {

        User user = getCurrentUser();
        List<Colis> colis;

        switch (user.getRole()) {

            case ROLE_ADMIN:
                colis = colisRepository.findAll();
                break;

            case ROLE_USER:
                colis = colisRepository.findByUser(user);
                break;

            case ROLE_VOYAGEUR:
                throw new BusinessException("Utilisez endpoint spécifique pour les colis disponibles");

            default:
                throw new BusinessException("Rôle non autorisé");
        }

        return colis.stream()
                .filter(c -> c.getStatut() != StatutColis.ANNULE)
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    public List<ColisResponseDTO> getAvailableColisForVoyageur(UUID trajetId) {

        User currentUser = getCurrentUser();

        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable"));


        if (!trajet.getVoyageur().getId().equals(currentUser.getId())) {
            throw new BusinessException("Accès refusé");
        }


        List<Colis> colisList = colisRepository
                .findByStatutAndVilleDepartAndVilleArrivee(
                        StatutColis.PUBLIE,
                        trajet.getOrigine(),
                        trajet.getDestination()
                );


        return colisList.stream()
                .filter(c -> c.getPoidsKg() <= trajet.getCapaciteKg())
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    public List<ColisResponseDTO> getMyColis(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        return colisRepository.findByUser(user)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }


    @Override
    public List<ColisResponseDTO> getPublicColis() {
        return colisRepository.findByStatut(StatutColis.PUBLIE)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }


    @Override
    public ColisResponseDTO getColisById(UUID id) {

        User currentUser = getCurrentUser();

        Colis colis = colisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colis introuvable"));

        if (currentUser.getRole() != Role.ROLE_ADMIN &&
                !colis.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Accès refusé");
        }

        return mapToResponseDTO(colis);
    }


    @Override
    public ColisResponseDTO startColis(UUID id) {

        User currentUser = getCurrentUser();

        Colis colis = colisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colis introuvable"));

        // ✅ check trajet
        if (colis.getTrajet() == null) {
            throw new BusinessException("Colis non assigné");
        }

        if (!colis.getTrajet().getVoyageur().getId().equals(currentUser.getId())) {
            throw new BusinessException("Accès refusé");
        }

        if (colis.getStatut() != StatutColis.ACCEPTE) {
            throw new BusinessException("Le colis doit être ACCEPTE");
        }

        colis.setStatut(StatutColis.EN_COURS);
        createTracking(
                colis,
                TrackingStatus.EN_ROUTE,
                "Le colis est en route",
                colis.getVilleDepart()
        );
        colisRepository.save(colis);

        // 🔔 notification
        notificationService.createNotification(
                colis.getUser(),
                "Votre colis est en cours de livraison ",
                TypeNotification.SYSTEME
        );

        return mapToResponseDTO(colis);
    }

    @Override
    public ColisResponseDTO finishColis(UUID id) {

        User currentUser = getCurrentUser();

        Colis colis = colisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colis introuvable"));


        if (colis.getTrajet() == null) {
            throw new BusinessException("Colis non assigné ❌");
        }

        // ✅ check voyageur
        if (colis.getTrajet().getVoyageur() == null) {
            throw new BusinessException("Voyageur introuvable ❌");
        }


        if (!colis.getTrajet().getVoyageur().getId().equals(currentUser.getId())) {
            throw new BusinessException("Accès refusé ❌");
        }


        if (colis.getStatut() != StatutColis.EN_COURS) {
            throw new BusinessException("Le colis doit être EN_COURS ❌");
        }


        colis.setStatut(StatutColis.LIVRE);
        createTracking(
                colis,
                TrackingStatus.LIVRE,
                "Colis livré avec succès",
                colis.getVilleArrivee()
        );

        colisRepository.save(colis);


        notificationService.createNotification(
                colis.getUser(),
                "Votre colis a été livré avec succès",
                TypeNotification.SYSTEME
        );

        notificationService.notifyAdmin("Colis livre", TypeNotification.SYSTEME);

        return mapToResponseDTO(colis);
    }

    @Override
    public ColisResponseDTO cancelColis(UUID id) {

        Colis colis = colisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colis introuvable"));

        if (colis.getStatut() == StatutColis.EN_COURS ||
                colis.getStatut() == StatutColis.LIVRE) {
            throw new BusinessException("Impossible d'annuler ce colis");
        }

        if (colis.getStatut() == StatutColis.ANNULE) {
            throw new BusinessException("Colis déjà annulé");
        }

        colis.setStatut(StatutColis.ANNULE);

        colisRepository.save(colis);

        return mapToResponseDTO(colis);
    }


    @Override
    public ColisResponseDTO deleteColis(UUID id) {

        User currentUser = getCurrentUser();

        Colis colis = colisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colis introuvable"));


        if (currentUser.getRole() != Role.ROLE_ADMIN &&
                !colis.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Accès refusé");
        }


        if (colis.getStatut() == StatutColis.LIVRE) {
            throw new BusinessException("Impossible de supprimer un colis livré");
        }


        if (colis.getStatut() == StatutColis.ANNULE) {
            throw new BusinessException("Colis déjà annulé");
        }


        colis.setStatut(StatutColis.ANNULE);

        colisRepository.save(colis);

        return mapToResponseDTO(colis);
    }


    @Override
    public ColisResponseDTO updateColis(UUID id, ColisRequestDTO dto) {

        User currentUser = getCurrentUser();

        Colis colis = colisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colis introuvable"));

        if (currentUser.getRole() != Role.ROLE_ADMIN &&
                !colis.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Accès refusé");
        }

        if (colis.getStatut() == StatutColis.LIVRE) {
            throw new BusinessException("Impossible de modifier un colis livré");
        }

        colis.setNom(dto.getNom());
        colis.setPoidsKg(dto.getPoidsKg());
        colis.setQuantite(dto.getQuantite());
        colis.setPrixProposeMRU(dto.getPrixProposeMRU());
        colis.setVilleDepart(dto.getVilleDepart());
        colis.setVilleArrivee(dto.getVilleArrivee());
        colis.setNomDestinataire(dto.getNomDestinataire());
        colis.setNumDestinataire(dto.getNumDestinataire());

        return mapToResponseDTO(colisRepository.save(colis));
    }


    @Override
    public ColisResponseDTO publishColis(UUID id) {

        User currentUser = getCurrentUser();

        Colis colis = colisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colis introuvable"));

        if (!colis.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Accès refusé");
        }

        if (colis.getStatut() != StatutColis.BROUILLON) {
            throw new BusinessException("Seul un colis brouillon peut être publié");
        }

        colis.setStatut(StatutColis.PUBLIE);
        createTracking(
                colis,
                TrackingStatus.COLIS_PUBLIE,
                "Le colis est maintenant public",
                colis.getVilleDepart()
        );

        notificationService.createNotification(
                currentUser,
                "Votre colis est maintenant public",
                TypeNotification.SYSTEME
        );

        return mapToResponseDTO(colisRepository.save(colis));
    }


    private Colis mapToEntity(ColisRequestDTO dto) {
        return Colis.builder()
                .nom(dto.getNom())
                .poidsKg(dto.getPoidsKg())
                .quantite(dto.getQuantite())
                .prixProposeMRU(dto.getPrixProposeMRU())
                .villeDepart(dto.getVilleDepart())
                .villeArrivee(dto.getVilleArrivee())
                .nomDestinataire(dto.getNomDestinataire())
                .numDestinataire(dto.getNumDestinataire())
                .statut(StatutColis.BROUILLON)
                .build();
    }

    private ColisResponseDTO mapToResponseDTO(Colis colis) {

        boolean isPaid = paiementRepository.existsByColis(colis);

        return ColisResponseDTO.builder()
                .id(colis.getId())
                .nom(colis.getNom())
                .poidsKg(colis.getPoidsKg())
                .quantite(colis.getQuantite())
                .prixProposeMRU(colis.getPrixProposeMRU())
                .statut(colis.getStatut().name())
                .trajetId(colis.getTrajet() != null ? colis.getTrajet().getId() : null)
                .villeDepart(colis.getVilleDepart())
                .villeArrivee(colis.getVilleArrivee())

                .paid(isPaid)
                .build();
    }
}