package com.tinkisqa.iam.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** DTOs del panel de administración (rol global ROLE_ADMIN). */
public final class AdminDtos {

    private AdminDtos() {
    }

    /** Resumen de cuenta para el listado de administración. */
    public record AdminUserResponse(
            UUID id,
            String email,
            String nombreCompleto,
            String role,
            String accountStatus,
            String identityStatus,
            String accessStatus,
            boolean emailVerified,
            boolean normasAceptadas,
            Instant createdAt) {
    }

    /** Página genérica de resultados. */
    public record PageResponse<T>(List<T> items, long total, int page, int size) {
    }

    public record SuspendRequest(String reason) {
    }

    public record RoleUpdateRequest(
            @NotBlank @Pattern(regexp = "ROLE_USER|ROLE_ADMIN", message = "Solo se admite ROLE_USER o ROLE_ADMIN.")
            String role) {
    }

    public record ReportResponse(
            UUID id,
            UUID reporterId,
            String targetType,
            UUID targetId,
            String reason,
            String status,
            UUID reviewerId,
            String resolution,
            Instant createdAt,
            Instant resolvedAt) {
    }

    public record ResolveReportRequest(
            @NotBlank @Pattern(regexp = "REVISADO|DESCARTADO", message = "La decisión debe ser REVISADO o DESCARTADO.")
            String decision,
            String notes) {
    }
}
