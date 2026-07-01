package com.livraison.backend.service;

import com.livraison.backend.dto.EmailRequestDto;
import com.livraison.backend.dto.RecipientDto;
import com.livraison.backend.dto.SenderDto;
import com.livraison.backend.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@Profile("prod")
public class BrevoEmailService implements EmailService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${BREVO_API_KEY}")
    private String apiKey;

    @Value("${BREVO_SENDER_EMAIL}")
    private String senderEmail;

    @Value("${BREVO_SENDER_NAME}")
    private String senderName;

    @Override
    public void sendOtpEmail(String to, String code) {

        try {
            String url = "https://api.brevo.com/v3/smtp/email";

            SenderDto sender = new SenderDto(senderName, senderEmail);
            RecipientDto recipient = new RecipientDto(to);

            EmailRequestDto request = new EmailRequestDto(
                    sender,
                    List.of(recipient),
                    "Votre code de vérification",
                    "Votre code OTP est: " + code
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            HttpEntity<EmailRequestDto> entity =
                    new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("[BREVO] Email sent: {}", response.getBody());

        } catch (Exception e) {
            log.error("[BREVO] Error sending email", e);
            throw new RuntimeException("Email sending failed");
        }
    }
}