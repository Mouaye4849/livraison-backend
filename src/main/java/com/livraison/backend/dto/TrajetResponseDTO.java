package com.livraison.backend.dto;

import com.livraison.backend.entity.TrajetStatut;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TrajetResponseDTO {

    private UUID id;
    private String origine;
    private String destination;
    private LocalDate dateDepart;
    private double capaciteKg;
    private TrajetStatut statut;
    private String voyageurEmail;

    private List<ColisResponseDTO> colis;
}
