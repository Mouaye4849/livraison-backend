package com.livraison.backend.auth;
import lombok.Data;

@Data
public class GoogleRequest {
    private String name;
    private String email;
    private String googleId;
}
