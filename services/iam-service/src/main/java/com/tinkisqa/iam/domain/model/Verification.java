package com.tinkisqa.iam.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Verificación de identidad solicitada por una usuaria.
 *
 * <p>Se guarda únicamente una <strong>huella no reversible</strong> del DNI
 * ({@code dniFingerprint}); nunca el número en claro ni fotografías. La huella
 * es única para evitar que un mismo documento se use en dos cuentas.</p>
 */
public record Verification(
        UUID id,
        UUID accountId,
        String dniFingerprint,
        String telefono,
        VerificationState state,
        String provider,
        Instant submittedAt,
        Instant resolvedAt,
        String notes) {

    public boolean isOpen() {
        return state == VerificationState.PENDIENTE || state == VerificationState.REVISION_HUMANA;
    }

    public Verification resolved(VerificationState newState, String reviewNotes, Instant now) {
        return new Verification(id, accountId, dniFingerprint, telefono, newState, provider,
                submittedAt, now, reviewNotes);
    }
}
