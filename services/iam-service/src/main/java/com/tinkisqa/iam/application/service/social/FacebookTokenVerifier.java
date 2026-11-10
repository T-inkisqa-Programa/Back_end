package com.tinkisqa.iam.application.service.social;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class FacebookTokenVerifier {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${social.facebook.app-id:}")
    private String appId;

    @Value("${social.facebook.app-secret:}")
    private String appSecret;

    public record FacebookPayload(String email, String name) {}

    public FacebookPayload verifyAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("accessToken inválido");
        }
        try {
            String url = "https://graph.facebook.com/me?fields=id,name,email&access_token=" + accessToken;
            Map res = restTemplate.getForObject(url, Map.class);
            if (res == null) throw new IllegalArgumentException("Token inválido");
            String email = (String) res.get("email");
            String name = (String) res.get("name");
            String id = (String) res.get("id");
            return new FacebookPayload(email != null ? email.toLowerCase() : id + "@facebook.local", name != null ? name : email);
        } catch (Exception e) {
            throw new IllegalArgumentException("No se pudo validar token de Facebook: " + e.getMessage());
        }
    }
}
