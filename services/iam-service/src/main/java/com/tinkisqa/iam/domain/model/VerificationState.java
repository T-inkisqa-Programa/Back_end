package com.tinkisqa.iam.domain.model;

/**
 * Estado de una verificación de identidad.
 *
 * <p>La verificación documental NUNCA pasa a {@link #VERIFICADA} de forma
 * automática: un proveedor simulado deriva a {@link #REVISION_HUMANA} y solo el
 * equipo de moderación puede resolverla.</p>
 */
public enum VerificationState {
    PENDIENTE,
    REVISION_HUMANA,
    VERIFICADA,
    RECHAZADA
}
