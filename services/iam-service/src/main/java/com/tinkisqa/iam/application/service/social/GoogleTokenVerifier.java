package com.tinkisqa.iam.application.service.social;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Component
public class GoogleTokenVerifier {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${social.google.client-id:}")
    private String googleClientId;

    public record GooglePayload(String email, String name) {}

    public GooglePayload verifyIdToken(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new IllegalArgumentException("idToken inválido");
        }
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            Map res = restTemplate.getForObject(url, Map.class);
            if (res == null) throw new IllegalArgumentException("Token inválido");
            String email = (String) res.get("email");
            String name = (String) res.get("name");
            String sub = (String) res.get("sub");
            return new GooglePayload(email != null ? email.toLowerCase() : sub + "@google.local", name != null ? name : email);
        } catch (Exception e) {
            throw new IllegalArgumentException("No se pudo validar token de Google: " + e.getMessage());
        }
    }
}
