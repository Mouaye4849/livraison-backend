package com.livraison.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;
    private String provider;
    private String googleId;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    // Web OTP verification — true for all existing/mobile/Google users so they
    // keep login access. Only web-registered accounts start as false until the
    // 6-digit code is confirmed.
    @Column(name = "email_verified", nullable = false, columnDefinition = "bit(1) default 1")
    private boolean emailVerified = true;

    @OneToMany(mappedBy = "user")
    private List<Colis> colis;
}