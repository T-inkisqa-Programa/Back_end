package com.tinkisqa.iam.domain.model;

/**
 * Identidad de género: SIEMPRE autodeclarada por la usuaria.
 * Nunca se usa el sexo registral del DNI como criterio.
 */
public enum GenderIdentity {
    MUJER,
    MUJER_TRANS,
    NO_BINARIA,
    PREFIERO_AUTODEFINIRME
}
