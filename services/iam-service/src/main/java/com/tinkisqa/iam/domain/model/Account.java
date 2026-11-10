package com.tinkisqa.iam.domain.model;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Agregado raíz de identidad: datos de la cuenta, credenciales, rol, estado de
 * la identidad/acceso y datos básicos del perfil.
 *
 * <p>Es un registro inmutable del dominio (sin anotaciones de framework). Los
 * cambios se expresan creando una nueva instancia mediante los métodos
 * {@code with...}.</p>
 */
public record Account(
        UUID id,
        String email,
        String passwordHash,
        String nombreCompleto,
        Role role,
        AccountStatus accountStatus,
        IdentityStatus identityStatus,
        AccessStatus accessStatus,
        boolean emailVerified,
        boolean normasAceptadas,
        String bio,
        GenderIdentity genero,
        String generoPersonalizado,
        Set<String> intereses,
        String avatarUri,
        java.time.Instant createdAt,
        java.time.Instant updatedAt) {

    public Account {
        intereses = intereses == null ? new LinkedHashSet<>() : new LinkedHashSet<>(intereses);
    }

    public static Account registered(
            UUID id,
            String email,
            String passwordHash,
            String nombreCompleto,
            java.time.Instant now) {
        return new Account(
                id,
                email,
                passwordHash,
                nombreCompleto,
                Role.ROLE_USER,
                AccountStatus.PENDIENTE_VERIFICACION,
                IdentityStatus.REGISTRADA,
                AccessStatus.ACCESO_PENDIENTE,
                false,
                false,
                null,
                null,
                null,
                Set.of(),
                null,
                now,
                now);
    }

    public Account withEmailVerified(java.time.Instant now) {
        return new Account(id, email, passwordHash, nombreCompleto, role, AccountStatus.ACTIVA,
                identityStatus, accessStatus, true, normasAceptadas, bio, genero, generoPersonalizado,
                intereses, avatarUri, createdAt, now);
    }

    public Account withIdentityStatus(IdentityStatus status, java.time.Instant now) {
        return new Account(id, email, passwordHash, nombreCompleto, role, accountStatus, status,
                accessStatus, emailVerified, normasAceptadas, bio, genero, generoPersonalizado,
                intereses, avatarUri, createdAt, now);
    }

    public Account withAccessStatus(AccessStatus status, java.time.Instant now) {
        return new Account(id, email, passwordHash, nombreCompleto, role, accountStatus, identityStatus,
                status, emailVerified, normasAceptadas, bio, genero, generoPersonalizado,
                intereses, avatarUri, createdAt, now);
    }

    public Account withNormasAceptadas(boolean accepted, java.time.Instant now) {
        return new Account(id, email, passwordHash, nombreCompleto, role, accountStatus, identityStatus,
                accessStatus, emailVerified, accepted, bio, genero, generoPersonalizado,
                intereses, avatarUri, createdAt, now);
    }

    public Account withAccountStatus(AccountStatus status, java.time.Instant now) {
        return new Account(id, email, passwordHash, nombreCompleto, role, status, identityStatus,
                accessStatus, emailVerified, normasAceptadas, bio, genero, generoPersonalizado,
                intereses, avatarUri, createdAt, now);
    }

    public Account withProfile(
            String nombreCompleto,
            String bio,
            GenderIdentity genero,
            String generoPersonalizado,
            Set<String> intereses,
            String avatarUri,
            java.time.Instant now) {
        return new Account(id, email, passwordHash,
                nombreCompleto == null ? this.nombreCompleto : nombreCompleto,
                role, accountStatus, identityStatus, accessStatus, emailVerified, normasAceptadas,
                bio, genero, generoPersonalizado,
                intereses == null ? this.intereses : intereses,
                avatarUri, createdAt, now);
    }

    public Account withRole(Role newRole, java.time.Instant now) {
        return new Account(id, email, passwordHash, nombreCompleto, newRole, accountStatus, identityStatus,
                accessStatus, emailVerified, normasAceptadas, bio, genero, generoPersonalizado,
                intereses, avatarUri, createdAt, now);
    }

    /** @return {@code true} si la cuenta ya puede iniciar sesión. */
    public boolean isLoginAllowed() {
        return accountStatus != AccountStatus.SUSPENDIDA;
    }

    public static Set<String> normalizeIntereses(Set<String> values) {
        if (values == null) {
            return Set.of();
        }
        return Arrays.stream(values.toArray(String[]::new))
                .filter(v -> v != null && !v.isBlank())
                .map(String::trim)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }
}
