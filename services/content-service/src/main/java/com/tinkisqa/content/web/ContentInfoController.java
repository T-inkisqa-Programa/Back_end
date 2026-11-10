package com.tinkisqa.content.web;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de verificación usado por el API Gateway para comprobar el
 * enrutamiento. Se completará con los casos de uso reales en la Fase 3.
 */
@RestController
@RequestMapping("/api/v1/articles")
public class ContentInfoController {

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "service", "content-service",
                "status", "UP",
                "timestamp", Instant.now().toString());
    }
}
