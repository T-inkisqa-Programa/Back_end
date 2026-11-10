package com.tinkisqa.social.application.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** DTOs del caso de uso de comunidades y solicitudes de ingreso. */
public final class CommunityDtos {

    private CommunityDtos() {
    }

    public record CommunityResponse(
            UUID id,
            String name,
            String description,
            String image,
            String category,
            String privacy,
            UUID ownerId,
            boolean userCreated,
            boolean owner,
            boolean member,
            boolean pendingRequest,
            long memberCount,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record CreateCommunityRequest(
            @NotBlank @Size(min = 3, max = 120) String name,
            @Size(max = 2000) String description,
            @Size(max = 2048) String image,
            @Size(max = 60) String category,
            @Pattern(regexp = "PUBLICA|PRIVADA", message = "La privacidad debe ser PUBLICA o PRIVADA.")
            String privacy) {
    }

    public record UpdateCommunityRequest(
            @Size(min = 3, max = 120) String name,
            @Size(max = 2000) String description,
            @Size(max = 2048) String image,
            @Size(max = 60) String category,
            @Pattern(regexp = "PUBLICA|PRIVADA", message = "La privacidad debe ser PUBLICA o PRIVADA.")
            String privacy) {
    }

    public record JoinRequestResponse(
            UUID id,
            UUID communityId,
            UUID accountId,
            String requesterName,
            String status,
            Instant requestedAt,
            Instant resolvedAt) {
    }
}
