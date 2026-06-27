package com.livraison.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserResponseDTO {

    private UUID id;
    private String email;
    private String role;
    private boolean enabled;
}