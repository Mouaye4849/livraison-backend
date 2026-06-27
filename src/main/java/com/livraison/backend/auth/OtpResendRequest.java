package com.livraison.backend.auth;

import lombok.Data;

@Data
public class OtpResendRequest {
    private String email;
}
