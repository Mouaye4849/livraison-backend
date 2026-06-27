package com.livraison.backend.auth;

import lombok.Data;

@Data
public class RegisterRequest {

    private String email;
    private String password;// VOYAGEUR / CLIENT / ADMIN
    private String name;
}
