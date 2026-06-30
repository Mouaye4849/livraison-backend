package com.livraison.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trajet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String origine;

    private String destination;

    private LocalDate dateDepart;

    private double capaciteKg;

    @Enumerated(EnumType.STRING)
    private TrajetStatut statut;

    @ManyToOne
    @JoinColumn(name = "voyageur_id", nullable = false)
    private User voyageur;

    @OneToMany(mappedBy = "trajet")
    private List<Colis> colis = new ArrayList<>();
}
