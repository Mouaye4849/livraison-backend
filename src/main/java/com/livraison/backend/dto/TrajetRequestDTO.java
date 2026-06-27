package com.livraison.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TrajetRequestDTO {

    private String origine;
    private String destination;
    private LocalDate dateDepart;
    private double capaciteKg;
}
