package com.livraison.backend.dto;

import com.livraison.backend.entity.StatutColis;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColisRequestDTO {

    @NotBlank(message = "Le nom est obligatoire")
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

    @NotBlank
    private String villeDepart;

    @NotBlank
    private String villeArrivee;

    @NotBlank
    private String nomDestinataire;

    @NotBlank
    private String numDestinataire;

    private StatutColis statut;
}