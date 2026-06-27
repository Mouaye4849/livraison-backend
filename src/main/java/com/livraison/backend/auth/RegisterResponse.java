package com.livraison.backend.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterResponse {
    private boolean requiresVerification;
    private String email;
    private String message;
}
