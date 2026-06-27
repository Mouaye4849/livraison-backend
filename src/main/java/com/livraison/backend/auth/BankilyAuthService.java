package com.livraison.backend.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class BankilyAuthService {

    private final WebClient webClient;

    @Value("${bankily.base-url}")
    private String baseUrl;

    @Value("${bankily.username}")
    private String username;

    @Value("${bankily.password}")
    private String password;

    @Value("${bankily.client-id}")
    private String clientId;

    public String getAccessToken() {

        BankilyAuthResponse response =
                webClient.post()
                        .uri(baseUrl + "/authentification")
                        .contentType(
                                MediaType.APPLICATION_FORM_URLENCODED
                        )
                        .bodyValue(
                                "grant_type=password"
                                        + "&username=" + username
                                        + "&password=" + password
                                        + "&client_id=" + clientId
                        )
                        .retrieve()
                        .bodyToMono(BankilyAuthResponse.class)
                        .block();

        return response.getAccessToken();

    }
}
