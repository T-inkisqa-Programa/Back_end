package com.tinkisqa.social.presentation.web;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de verificación usado por el API Gateway para comprobar el
 * enrutamiento.
 */
@RestController
@RequestMapping("/api/v1/communities")
public class SocialInfoController {

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "service", "social-service",
                "status", "UP",
                "timestamp", Instant.now().toString());
    }
}
