package com.tinkisqa.iam.domain.service;

import java.util.EnumSet;
import java.util.Set;

import com.tinkisqa.iam.domain.model.AccessStatus;
import com.tinkisqa.iam.domain.model.IdentityStatus;
import com.tinkisqa.iam.domain.model.Permission;

/**
 * Regla de negocio pura para los permisos funcionales de la app.
 * La revisión de identidad es un flujo de moderación, no una barrera de acceso.
 */
public final class PermissionRules {

    private PermissionRules() {
    }

    public static Set<Permission> permissionsFor(IdentityStatus identityStatus, AccessStatus accessStatus) {
        // Mantener los parámetros en el contrato: los estados siguen describiendo
        // la revisión, pero no bloquean el uso normal de la app.
        if (identityStatus == null || accessStatus == null) {
            throw new IllegalArgumentException("Los estados de identidad y acceso son obligatorios.");
        }

        // Una suspensión administrativa es la única restricción general.
        if (accessStatus == AccessStatus.CUENTA_SUSPENDIDA) {
            return EnumSet.of(Permission.VER_ESTADO_SOLICITUD);
        }

        // Toda usuaria activa puede utilizar las funciones de la app; la
        // verificación pendiente solo limita la insignia y queda para revisión.
        return EnumSet.allOf(Permission.class);
    }
}
