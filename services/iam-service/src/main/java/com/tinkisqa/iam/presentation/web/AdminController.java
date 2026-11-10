package com.tinkisqa.iam.presentation.web;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tinkisqa.iam.application.dto.AdminDtos;
import com.tinkisqa.iam.application.dto.VerificationDtos;
import com.tinkisqa.iam.application.service.AdminModerationService;
import com.tinkisqa.iam.application.service.AdminUserService;
import com.tinkisqa.iam.domain.exception.DomainException;
import com.tinkisqa.iam.domain.model.AccountStatus;
import com.tinkisqa.iam.domain.model.ReportStatus;
import com.tinkisqa.iam.presentation.security.CurrentUser;

import jakarta.validation.Valid;

/**
 * Panel de administración (rol global {@code ROLE_ADMIN}).
 *
 * <p>Toda la clase está protegida a nivel de método: aunque el API Gateway ya
 * valida el JWT, el propio servicio lo vuelve a validar y exige el rol. Es
 * defensa en profundidad: el permiso se decide siempre en el backend.</p>
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminUserService adminUserService;
    private final AdminModerationService adminModerationService;
    private final CurrentUser currentUser;

    public AdminController(
            AdminUserService adminUserService,
            AdminModerationService adminModerationService,
            CurrentUser currentUser) {
        this.adminUserService = adminUserService;
        this.adminModerationService = adminModerationService;
        this.currentUser = currentUser;
    }

    // ------------------------------------------------------------------ usuarios

    @GetMapping("/users")
    public AdminDtos.PageResponse<AdminDtos.AdminUserResponse> users(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminUserService.users(query, parseAccountStatus(status), page, size);
    }

    @GetMapping("/users/{id}")
    public AdminDtos.AdminUserResponse user(@PathVariable UUID id) {
        return adminUserService.user(id);
    }

    @PostMapping("/users/{id}/suspend")
    public AdminDtos.AdminUserResponse suspend(
            @PathVariable UUID id,
            @RequestBody(required = false) AdminDtos.SuspendRequest request) {
        String reason = request == null ? null : request.reason();
        return adminUserService.suspend(currentUser.requireAccountId(), id, reason);
    }

    @PostMapping("/users/{id}/reactivate")
    public AdminDtos.AdminUserResponse reactivate(@PathVariable UUID id) {
        return adminUserService.reactivate(currentUser.requireAccountId(), id);
    }

    @PostMapping("/users/{id}/role")
    public AdminDtos.AdminUserResponse changeRole(
            @PathVariable UUID id,
            @Valid @RequestBody AdminDtos.RoleUpdateRequest request) {
        return adminUserService.changeRole(currentUser.requireAccountId(), id, request.role());
    }

    // ------------------------------------------------------------- verificaciones

    @GetMapping("/verifications")
    public List<VerificationDtos.AdminVerificationResponse> verifications(
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "50") int limit) {
        return adminModerationService.verifications(state, limit);
    }

    // ----------------------------------------------------------------- denuncias

    @GetMapping("/reports")
    public AdminDtos.PageResponse<AdminDtos.ReportResponse> reports(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminModerationService.reports(parseReportStatus(status), page, size);
    }

    @PostMapping("/reports/{id}/resolve")
    public AdminDtos.ReportResponse resolveReport(
            @PathVariable UUID id,
            @Valid @RequestBody AdminDtos.ResolveReportRequest request) {
        return adminModerationService.resolveReport(currentUser.requireAccountId(), id, request);
    }

    // ------------------------------------------------------------------ helpers

    private static AccountStatus parseAccountStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return AccountStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw DomainException.validation(
                    "Estado no válido. Valores permitidos: PENDIENTE_VERIFICACION, ACTIVA, SUSPENDIDA.");
        }
    }

    private static ReportStatus parseReportStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return ReportStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw DomainException.validation("Estado no válido. Valores permitidos: ABIERTO, REVISADO, DESCARTADO.");
        }
    }
}
