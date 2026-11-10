package com.tinkisqa.iam.infrastructure.verification;

import org.springframework.stereotype.Component;

import com.tinkisqa.iam.domain.port.IdentityVerificationPort;
import com.tinkisqa.iam.domain.service.AccountRules;

/**
 * Adaptador de verificación documental por <strong>revisión humana</strong>.
 *
 * <p>Verifica únicamente el formato del documento y deriva SIEMPRE la decisión
 * a una persona del equipo. No se simula ninguna integración real (RENIEC u
 * otra): el servicio nunca marca una identidad como verificada por sí mismo.</p>
 */
@Component
public class ManualReviewIdentityVerificationAdapter implements IdentityVerificationPort {

    private static final String PROVIDER = "revision-manual";

    @Override
    public Result verify(String dni, String telefono, String selfieReference) {
        boolean formatValid = AccountRules.isDniValid(dni);
        if (!formatValid) {
            return new Result(Outcome.RECHAZADO, false, PROVIDER,
                    "El documento no tiene un formato válido (8 dígitos).");
        }
        return new Result(Outcome.REVISION_HUMANA, true, PROVIDER,
                "Formato válido. Pendiente de revisión humana: el sistema no verifica "
                        + "automáticamente la autenticidad ni la titularidad del documento.");
    }
}
