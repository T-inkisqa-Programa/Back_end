package com.tinkisqa.iam.application.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

/** DTOs de seguridad (eventos, reportes y bloqueos). */
public final class SecurityDtos {

    private SecurityDtos() {
    }

    public record ReportRequest(
            @NotBlank String targetUserId,
            @NotBlank String reason) {
    }

    public record BlockRequest(
            @NotBlank String userId) {
    }

    public record SecurityEventResponse(
            UUID id,
            String type,
            String detail,
            java.time.Instant createdAt) {
    }
}
