package com.livraison.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private Double montantMRU;

    @Enumerated(EnumType.STRING)
    private StatutPaiement statut;

    private LocalDateTime dateConfirmation;

    @Column(nullable = false)
    private Double commissionAdmin;

    @Column(nullable = false)
    private Double montantVoyageur;

    @Enumerated(EnumType.STRING)
    private TypePaiement typePaiement;

    @ManyToOne
    private Colis colis;

    @ManyToOne
    private User voyageur;

    @ManyToOne
    private User payeur;

    private String operationId;

    private String referenceTransaction;

    private String errorCode;

    private String errorMessage;
}
