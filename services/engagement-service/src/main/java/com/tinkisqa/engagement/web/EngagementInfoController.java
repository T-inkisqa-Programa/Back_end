package com.tinkisqa.engagement.web;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de verificación usado por el API Gateway para comprobar el
 * enrutamiento. Se completará con los casos de uso reales en la Fase 5.
 */
@RestController
@RequestMapping("/api/v1/streaks")
public class EngagementInfoController {

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "service", "engagement-service",
                "status", "UP",
                "timestamp", Instant.now().toString());
    }
}
