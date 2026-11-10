package com.tinkisqa.iam.application.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** DTOs de verificación de identidad y de teléfono. */
public final class VerificationDtos {

    private VerificationDtos() {
    }

    public record SubmitVerificationRequest(
            @NotBlank @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos.") String dni,
            @NotBlank @Pattern(regexp = "\\d{9}", message = "El teléfono debe tener 9 dígitos.") String telefono,
            String selfieReference) {
    }

    public record VerificationResponse(
            UUID id,
            String state,
            String provider,
            Instant submittedAt,
            Instant resolvedAt,
            String notes,
            boolean open) {
    }

    /** Datos mínimos que el panel administrativo necesita para revisar la cola. */
    public record AdminVerificationResponse(
            UUID id,
            UUID accountId,
            String email,
            String nombreCompleto,
            String state,
            String provider,
            Instant submittedAt,
            boolean open) {
    }

    public record ReviewVerificationRequest(
            @NotBlank @Pattern(regexp = "VERIFICADA|RECHAZADA", message = "La decisión debe ser VERIFICADA o RECHAZADA.")
            String decision,
            String notes) {
    }

    public record SendPhoneOtpRequest(
            @NotBlank @Pattern(regexp = "\\d{9}", message = "El teléfono debe tener 9 dígitos.") String telefono) {
    }

    public record VerifyPhoneOtpRequest(
            @NotBlank @Pattern(regexp = "\\d{9}", message = "El teléfono debe tener 9 dígitos.") String telefono,
            @NotBlank String code) {
    }

    public record OtpSentResponse(
            String target,
            String channel,
            long expiresInSeconds,
            long resendCooldownSeconds) {
    }
}
