package com.tinkisqa.iam.application.dto;

import java.util.List;
import java.util.UUID;

/** DTOs de perfil, permisos y estado de acceso. */
public final class ProfileDtos {

    private ProfileDtos() {
    }

    public record ProfileResponse(
            UUID id,
            String email,
            String nombreCompleto,
            String bio,
            String genero,
            String generoPersonalizado,
            List<String> intereses,
            String avatarUri,
            String accountStatus,
            String identityStatus,
            String accessStatus,
            List<String> roles,
            boolean emailVerified,
            boolean normasAceptadas,
            List<String> permissions) {
    }

    public record UpdateProfileRequest(
            String nombreCompleto,
            String bio,
            String genero,
            String generoPersonalizado,
            List<String> intereses,
            String avatarUri) {
    }

    public record PermissionsResponse(List<String> permissions) {
    }

    public record NormDto(String title, String description) {
    }

    public record AdmissionCriterionDto(String key, String label, boolean satisfied) {
    }

    public record AccessResponse(
            String status,
            boolean admitted,
            List<AdmissionCriterionDto> criteria,
            List<NormDto> norms) {
    }
}
