package com.livraison.backend.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "colis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Colis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private String nom;

    @NotNull
    @Positive
    private Double poidsKg;

    @NotNull
    @Positive
    private Integer quantite;

    @NotNull
    @Positive
    private Double prixProposeMRU;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private StatutColis statut = StatutColis.BROUILLON;

    private LocalDate dateDebutSouhaitee;
    private LocalDate dateFinSouhaitee;

    private String villeDepart;
    private String villeArrivee;

    private String nomDestinataire;
    private String numDestinataire;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "trajet_id")
    private Trajet trajet;

    @Enumerated(EnumType.STRING)
    private TrackingStatus trackingStatus;
}
