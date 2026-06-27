package com.livraison.backend.auth;

import lombok.Data;

@Data
public class OtpVerifyRequest {
    private String email;
    private String code;
}
