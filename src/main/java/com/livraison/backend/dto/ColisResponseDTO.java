package com.livraison.backend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColisResponseDTO {

    private UUID id;
    private UUID trajetId;
    private String nom;
    private Double poidsKg;
    private Integer quantite;
    private Double prixProposeMRU;
    private String statut;
    private String villeDepart;
    private String villeArrivee;
    private boolean paid;
}