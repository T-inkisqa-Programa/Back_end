package com.tinkisqa.iam.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tinkisqa.iam.application.dto.AdminDtos;
import com.tinkisqa.iam.domain.exception.DomainException;
import com.tinkisqa.iam.domain.model.Account;
import com.tinkisqa.iam.domain.model.AccountStatus;
import com.tinkisqa.iam.domain.model.AccessStatus;
import com.tinkisqa.iam.domain.model.Role;
import com.tinkisqa.iam.domain.model.SecurityEvent;
import com.tinkisqa.iam.domain.model.SecurityEventType;
import com.tinkisqa.iam.domain.port.AccountRepository;
import com.tinkisqa.iam.domain.port.RefreshTokenRepository;
import com.tinkisqa.iam.domain.port.SecurityEventRepository;

/**
 * Casos de uso de administración de usuarias (rol global {@code ROLE_ADMIN}).
 *
 * <p>No gestiona contenido: la moderación de publicaciones pertenece al Social
 * Service. Aquí solo se gobiernan las cuentas y sus estados.</p>
 */
@Service
public class AdminUserService {

    private static final int MAX_PAGE_SIZE = 100;

    private final AccountRepository accountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecurityEventRepository securityEventRepository;
    private final AdmissionService admissionService;
    private final java.time.Clock clock;

    public AdminUserService(
            AccountRepository accountRepository,
            RefreshTokenRepository refreshTokenRepository,
            SecurityEventRepository securityEventRepository,
            AdmissionService admissionService,
            java.time.Clock clock) {
        this.accountRepository = accountRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.securityEventRepository = securityEventRepository;
        this.admissionService = admissionService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public AdminDtos.PageResponse<AdminDtos.AdminUserResponse> users(
            String query, AccountStatus status, int page, int size) {
        int safeSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        int safePage = Math.max(0, page);
        List<AdminDtos.AdminUserResponse> items = accountRepository.search(query, status, safePage, safeSize).stream()
                .map(AdminUserService::toResponse)
                .toList();
        long total = accountRepository.count(query, status);
        return new AdminDtos.PageResponse<>(items, total, safePage, safeSize);
    }

    @Transactional(readOnly = true)
    public AdminDtos.AdminUserResponse user(UUID userId) {
        return toResponse(load(userId));
    }

    /** Suspende la cuenta: corta el acceso y revoca sus tokens de refresco. */
    @Transactional
    public AdminDtos.AdminUserResponse suspend(UUID adminId, UUID userId, String reason) {
        if (adminId.equals(userId)) {
            throw DomainException.forbidden("No puedes suspender tu propia cuenta.");
        }
        Account account = load(userId);
        if (account.accountStatus() == AccountStatus.SUSPENDIDA) {
            return toResponse(account);
        }
        Instant now = clock.instant();
        Account saved = accountRepository.save(account
                .withAccountStatus(AccountStatus.SUSPENDIDA, now)
                .withAccessStatus(AccessStatus.CUENTA_SUSPENDIDA, now));
        refreshTokenRepository.revokeAllForAccount(userId, now);
        securityEventRepository.save(SecurityEvent.of(userId, SecurityEventType.CUENTA_SUSPENDIDA,
                "Suspendida por " + adminId + describeReason(reason), now));
        return toResponse(saved);
    }

    /** Reactiva la cuenta y recalcula su acceso a la comunidad. */
    @Transactional
    public AdminDtos.AdminUserResponse reactivate(UUID adminId, UUID userId) {
        Account account = load(userId);
        Instant now = clock.instant();
        Account active = account
                .withAccountStatus(AccountStatus.ACTIVA, now)
                .withAccessStatus(AccessStatus.ACCESO_PENDIENTE, now);
        Account saved = accountRepository.save(admissionService.recompute(active, now));
        securityEventRepository.save(SecurityEvent.of(userId, SecurityEventType.CUENTA_REACTIVADA,
                "Reactivada por " + adminId, now));
        return toResponse(saved);
    }

    /**
     * Actualiza roles sin permitir crear nuevos administradores desde la API.
     * Los cambios de rol revocan los refresh tokens para surtir efecto al volver
     * a iniciar sesión.
     */
    @Transactional
    public AdminDtos.AdminUserResponse changeRole(UUID adminId, UUID userId, String roleName) {
        if (adminId.equals(userId)) {
            throw DomainException.forbidden("No puedes cambiar tu propio rol.");
        }
        Role role = parseRole(roleName);
        Account account = load(userId);
        if (role == Role.ROLE_ADMIN && account.role() != Role.ROLE_ADMIN) {
            throw DomainException.forbidden("No se pueden crear cuentas administrativas desde la API.");
        }
        if (account.role() == role) {
            return toResponse(account);
        }
        Instant now = clock.instant();
        Account saved = accountRepository.save(account.withRole(role, now));
        refreshTokenRepository.revokeAllForAccount(userId, now);
        securityEventRepository.save(SecurityEvent.of(userId, SecurityEventType.ROL_ACTUALIZADO,
                "Rol " + role.name() + " asignado por " + adminId, now));
        return toResponse(saved);
    }

    private Account load(UUID userId) {
        return accountRepository.findById(userId)
                .orElseThrow(() -> DomainException.notFound("La cuenta no existe."));
    }

    private static String describeReason(String reason) {
        return reason == null || reason.isBlank() ? "" : ": " + reason.trim();
    }

    private static Role parseRole(String raw) {
        if (raw == null || raw.isBlank()) {
            throw DomainException.validation("Debes indicar el rol.");
        }
        try {
            return Role.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw DomainException.validation("Rol no válido: solo se admite ROLE_USER o ROLE_ADMIN.");
        }
    }

    private static AdminDtos.AdminUserResponse toResponse(Account account) {
        return new AdminDtos.AdminUserResponse(
                account.id(),
                account.email(),
                account.nombreCompleto(),
                account.role().name(),
                account.accountStatus().name(),
                account.identityStatus().name(),
                account.accessStatus().name(),
                account.emailVerified(),
                account.normasAceptadas(),
                account.createdAt());
    }
}
