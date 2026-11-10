package com.tinkisqa.iam.domain.port;

/**
 * Puerto de salida: verificación documental de identidad.
 *
 * <p><strong>Importante:</strong> no se simula una integración real (p. ej.
 * RENIEC). La única implementación disponible deriva siempre a
 * {@link Outcome#REVISION_HUMANA}: confirma que el documento tiene formato
 * válido, pero nunca afirma que sea auténtico ni que pertenezca a la usuaria.
 * La decisión final es humana y trazable.</p>
 */
public interface IdentityVerificationPort {

    enum Outcome {
        /** El documento requiere revisión por el equipo. */
        REVISION_HUMANA,
        /** El documento no supera la validación básica. */
        RECHAZADO
    }

    record Result(Outcome outcome, boolean documentFormatValid, String provider, String reason) {
    }

    Result verify(String dni, String telefono, String selfieReference);
}
