package com.livraison.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String to, String code) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject("Votre code de vérification WASALI");
            msg.setText(
                    "Bonjour,\n\n" +
                            "Votre code de vérification WASALI est :\n\n" +
                            "        " + code + "\n\n" +
                            "Ce code est valide pendant 10 minutes.\n\n" +
                            "Si vous n'avez pas demandé ce code, ignorez cet email.\n\n" +
                            "— L'équipe WASALI"
            );
            mailSender.send(msg);
            log.info("[OTP] Email sent to {}", to);
        } catch (Exception e) {
            log.error("[OTP] Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'email de vérification. Vérifiez votre adresse.");
        }
    }
}
