package com.livraison.backend.service;

import com.livraison.backend.dto.ColisResponseDTO;
import com.livraison.backend.dto.TrajetRequestDTO;
import com.livraison.backend.dto.TrajetResponseDTO;
import com.livraison.backend.entity.*;
import com.livraison.backend.exception.BusinessException;
import com.livraison.backend.exception.ResourceNotFoundException;
import com.livraison.backend.repository.ColisRepository;
import com.livraison.backend.repository.TrajetRepository;
import com.livraison.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TrajetServiceImpl implements TrajetService {

    private final TrajetRepository trajetRepository;
    private final UserRepository userRepository;
    private final ColisRepository colisRepository;
    private final NotificationService notificationService;
    private final ChatService chatService;


    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }


    @Override
    public List<TrajetResponseDTO> getPendingTrajets() {

        User user = getCurrentUser();

        if (user.getRole() != Role.ROLE_ADMIN) {
            throw new BusinessException("Accès refusé ❌");
        }

        return trajetRepository.findByStatut(TrajetStatut.EN_ATTENTE)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }


    @Override
    public void approveTrajet(UUID id) {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ROLE_ADMIN) {
            throw new BusinessException("Accès refusé ");
        }

        Trajet trajet = trajetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable"));

        if (trajet.getStatut() != TrajetStatut.EN_ATTENTE) {
            throw new BusinessException("Trajet déjà traité");
        }

        trajet.setStatut(TrajetStatut.OUVERT);
        trajetRepository.save(trajet);

        // 📩 Notify voyageur
        notificationService.createNotification(
                trajet.getVoyageur(),
                " Votre trajet a été validé",
                TypeNotification.SYSTEME
        );
    }


    @Override
    public void rejectTrajet(UUID id) {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ROLE_ADMIN) {
            throw new BusinessException("Accès refusé ❌");
        }

        Trajet trajet = trajetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable"));

        if (trajet.getStatut() != TrajetStatut.EN_ATTENTE) {
            throw new BusinessException("Trajet déjà traité");
        }

        trajet.setStatut(TrajetStatut.REFUSE);
        trajetRepository.save(trajet);


        notificationService.createNotification(
                trajet.getVoyageur(),
                " Votre trajet a été refusé",
                TypeNotification.SYSTEME
        );
    }


    @Override
    public TrajetResponseDTO createTrajet(TrajetRequestDTO dto) {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ROLE_VOYAGEUR) {
            throw new BusinessException("Seul un voyageur peut créer un trajet");
        }

        if (dto.getDateDepart().isBefore(LocalDate.now())) {
            throw new BusinessException("La date ne peut pas être passée");
        }

        Trajet trajet = Trajet.builder()
                .origine(dto.getOrigine())
                .destination(dto.getDestination())
                .dateDepart(dto.getDateDepart())
                .capaciteKg(dto.getCapaciteKg())
                .statut(TrajetStatut.EN_ATTENTE)
                .voyageur(currentUser)
                .build();


        Trajet saved = trajetRepository.save(trajet);


        notificationService.notifyAdmin(
                " Nouveau trajet en attente de validation",
                TypeNotification.SYSTEME
        );

        return mapToDTO(saved);
    }


    @Override
    public List<TrajetResponseDTO> getMyTrajets() {

        User user = getCurrentUser();

        if (user.getRole() != Role.ROLE_VOYAGEUR) {
            throw new BusinessException("Accès réservé aux voyageurs");
        }

        return trajetRepository.findByVoyageur(user)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }


    @Override
    public TrajetResponseDTO getTrajetById(UUID id) {

        User user = getCurrentUser();

        Trajet trajet = trajetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable"));

        if (user.getRole() != Role.ROLE_ADMIN &&
                !trajet.getVoyageur().getId().equals(user.getId())) {
            throw new BusinessException("Accès refusé");
        }

        return mapToDTO(trajet);
    }


    @Override
    public List<TrajetResponseDTO> getAllTrajets() {

        User user = getCurrentUser();
        List<Trajet> trajets;

        switch (user.getRole()) {

            case ROLE_ADMIN:
                trajets = trajetRepository.findAll();
                break;

            case ROLE_VOYAGEUR:
                trajets = trajetRepository.findByVoyageur(user);
                break;

            default:
                throw new BusinessException("Accès refusé ❌");
        }

        return trajets.stream()
                .map(this::mapToDTO)
                .toList();
    }


    @Override
    public List<TrajetResponseDTO> getOpenTrajets() {
        return trajetRepository.findByStatut(TrajetStatut.OUVERT)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }


    @Override
    public void deleteTrajet(UUID id) {

        User currentUser = getCurrentUser();

        Trajet trajet = trajetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable"));

        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;
        boolean isOwner = trajet.getVoyageur().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new BusinessException("Accès refusé ❌");
        }

        // Nobody can delete a trajet that is actively delivering
        if (trajet.getStatut() == TrajetStatut.EN_COURS) {
            throw new BusinessException("Impossible de supprimer un trajet en cours de livraison ❌");
        }

        // Owner (non-admin) is blocked only when there are colis already accepted
        // or being delivered — statuses that represent committed business operations.
        // Admin can force-delete in any other situation.
        if (!isAdmin) {
            boolean hasActiveColis = trajet.getColis() != null &&
                    trajet.getColis().stream().anyMatch(c ->
                            c.getStatut() == StatutColis.ACCEPTE ||
                                    c.getStatut() == StatutColis.EN_COURS);
            if (hasActiveColis) {
                throw new BusinessException("Trajet contient des colis actifs — annulez-les d'abord ❌");
            }
        }

        // Unlink colis from this trajet before deletion to satisfy the FK constraint.
        // (Colis.trajet_id is nullable; @OneToMany has no cascade/orphanRemoval.)
        if (trajet.getColis() != null && !trajet.getColis().isEmpty()) {
            trajet.getColis().forEach(c -> c.setTrajet(null));
            colisRepository.saveAll(trajet.getColis());
        }

        trajetRepository.delete(trajet);

        notificationService.createNotification(
                trajet.getVoyageur(),
                " Votre trajet a été supprimé",
                TypeNotification.SYSTEME
        );
    }

    @Override
    public TrajetResponseDTO updateTrajet(UUID id, TrajetRequestDTO dto) {

        User currentUser = getCurrentUser();

        Trajet trajet = trajetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable"));


        if (currentUser.getRole() != Role.ROLE_ADMIN &&
                !trajet.getVoyageur().getId().equals(currentUser.getId())) {
            throw new BusinessException("Accès refusé ❌");
        }


        if (trajet.getStatut() == TrajetStatut.EN_ATTENTE) {
            throw new BusinessException("Trajet en attente de validation");
        }

        if (trajet.getStatut() == TrajetStatut.COMPLET) {
            throw new BusinessException("Trajet complet ❌");
        }


        trajet.setOrigine(dto.getOrigine());
        trajet.setDestination(dto.getDestination());
        trajet.setDateDepart(dto.getDateDepart());
        trajet.setCapaciteKg(dto.getCapaciteKg());

        return mapToDTO(trajetRepository.save(trajet));
    }


    @Override
    public void acceptColis(UUID trajetId, UUID colisId) {

        User currentUser = getCurrentUser();

        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable"));

        if (!trajet.getVoyageur().getId().equals(currentUser.getId())) {
            throw new BusinessException("Accès refusé");
        }

        if (trajet.getStatut() != TrajetStatut.OUVERT) {
            throw new BusinessException("Trajet non validé par admin");
        }

        Colis colis = colisRepository.findById(colisId)
                .orElseThrow(() -> new ResourceNotFoundException("Colis introuvable"));

        if (colis.getStatut() != StatutColis.PUBLIE) {
            throw new BusinessException("Colis non disponible");
        }

        if (trajet.getCapaciteKg() < colis.getPoidsKg()) {
            throw new BusinessException("Capacité insuffisante");
        }

        colis.setTrajet(trajet);
        colis.setStatut(StatutColis.ACCEPTE);

        trajet.setCapaciteKg(trajet.getCapaciteKg() - colis.getPoidsKg());

        if (trajet.getCapaciteKg() <= 0) {
            trajet.setStatut(TrajetStatut.COMPLET);
        }

        colisRepository.save(colis);
        trajetRepository.save(trajet);

        ChatMessage msg = new ChatMessage();
        msg.setColisId(colis.getId());
        msg.setSenderEmail("SYSTEM");
        msg.setContent("💬 تم قبول الطلب، يمكنك بدء المحادثة مع voyageur");

        chatService.save(msg);
    }


    private TrajetResponseDTO mapToDTO(Trajet trajet) {

        return TrajetResponseDTO.builder()
                .id(trajet.getId())
                .origine(trajet.getOrigine())
                .destination(trajet.getDestination())
                .dateDepart(trajet.getDateDepart())
                .capaciteKg(trajet.getCapaciteKg())
                .statut(trajet.getStatut())
                .voyageurEmail(trajet.getVoyageur().getEmail())

                .colis(
                        trajet.getColis() != null
                                ? trajet.getColis().stream()
                                .map(c -> ColisResponseDTO.builder()
                                        .id(c.getId())
                                        .nom(c.getNom())
                                        .poidsKg(c.getPoidsKg())
                                        .statut(c.getStatut().name())
                                        .build()
                                ).toList()
                                : List.of()
                )
                .build();
    }
}