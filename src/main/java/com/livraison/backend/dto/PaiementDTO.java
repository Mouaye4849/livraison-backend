package com.livraison.backend.dto;

import com.livraison.backend.entity.StatutPaiement;
import com.livraison.backend.entity.TypePaiement;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class PaiementDTO {

    private UUID id;
    private Double montantMRU;
    private StatutPaiement statut;
    private TypePaiement typePaiement;
    private String referenceTransaction;
    private LocalDateTime dateConfirmation;
    private Double commissionAdmin;
    private Double montantVoyageur;
}