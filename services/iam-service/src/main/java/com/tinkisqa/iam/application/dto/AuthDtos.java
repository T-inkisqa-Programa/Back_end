package com.tinkisqa.iam.application.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTOs del caso de uso de autenticación. */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 10, max = 128) String password,
            @NotBlank @Size(min = 3, max = 140) String nombreCompleto,
            @AssertTrue(message = "Debes aceptar los términos y condiciones.") boolean acceptTerms) {
    }

    public record RegisterResponse(
            String email,
            String message,
            long otpExpiresInSeconds,
            long resendCooldownSeconds) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    public record LogoutRequest(@NotBlank String refreshToken) {
    }

    public record VerifyEmailRequest(
            @NotBlank @Email String email,
            @NotBlank String code) {
    }

    public record ResendOtpRequest(@NotBlank @Email String email) {
    }

    public record UserSummary(
            UUID id,
            String email,
            String nombreCompleto,
            String role,
            String identityStatus,
            String accessStatus,
            boolean emailVerified,
            List<String> permissions) {
    }

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresInSeconds,
            UserSummary user) {
    }

    public record SocialLoginRequest(
            @NotBlank String provider,
            String idToken,
            String accessToken) {
    }
}
