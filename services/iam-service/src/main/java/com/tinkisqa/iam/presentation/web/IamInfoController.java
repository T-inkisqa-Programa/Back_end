package com.tinkisqa.iam.presentation.web;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de verificación usado por el API Gateway para comprobar el
 * enrutamiento hacia el IAM.
 */
@RestController
public class IamInfoController {

    @GetMapping("/api/v1/auth/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "service", "iam-service",
                "status", "UP",
                "timestamp", Instant.now().toString());
    }
}
