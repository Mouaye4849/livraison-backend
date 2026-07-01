package com.livraison.backend.service;

public interface EmailService {

    void sendOtpEmail(String to, String code);
}