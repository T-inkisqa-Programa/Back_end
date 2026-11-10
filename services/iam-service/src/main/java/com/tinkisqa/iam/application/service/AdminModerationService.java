package com.tinkisqa.iam.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tinkisqa.iam.application.dto.AdminDtos;
import com.tinkisqa.iam.application.dto.VerificationDtos;
import com.tinkisqa.iam.domain.exception.DomainException;
import com.tinkisqa.iam.domain.model.Report;
import com.tinkisqa.iam.domain.model.ReportStatus;
import com.tinkisqa.iam.domain.model.SecurityEvent;
import com.tinkisqa.iam.domain.model.SecurityEventType;
import com.tinkisqa.iam.domain.model.Verification;
import com.tinkisqa.iam.domain.model.VerificationState;
import com.tinkisqa.iam.domain.port.AccountRepository;
import com.tinkisqa.iam.domain.port.ReportRepository;
import com.tinkisqa.iam.domain.port.SecurityEventRepository;
import com.tinkisqa.iam.domain.port.VerificationRepository;

/**
 * Casos de uso de moderación (rol global {@code ROLE_ADMIN}): cola de
 * verificaciones de identidad y gestión de denuncias contra usuarias.
 */
@Service
public class AdminModerationService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;
    private static final int MAX_PAGE_SIZE = 100;

    private final VerificationRepository verificationRepository;
    private final AccountRepository accountRepository;
    private final ReportRepository reportRepository;
    private final SecurityEventRepository securityEventRepository;
    private final java.time.Clock clock;

    public AdminModerationService(
            VerificationRepository verificationRepository,
            AccountRepository accountRepository,
            ReportRepository reportRepository,
            SecurityEventRepository securityEventRepository,
            java.time.Clock clock) {
        this.verificationRepository = verificationRepository;
        this.accountRepository = accountRepository;
        this.reportRepository = reportRepository;
        this.securityEventRepository = securityEventRepository;
        this.clock = clock;
    }

    /**
     * Cola de verificación de identidad. Sin {@code state} devuelve las que
     * están abiertas (PENDIENTE + REVISION_HUMANA).
     */
    @Transactional(readOnly = true)
    public List<VerificationDtos.AdminVerificationResponse> verifications(String state, int limit) {
        int safeLimit = Math.min(Math.max(1, limit <= 0 ? DEFAULT_LIMIT : limit), MAX_LIMIT);
        List<Verification> found = state == null || state.isBlank()
                ? verificationRepository.findOpen(safeLimit)
                : verificationRepository.findByState(parseState(state), safeLimit);
        return found.stream().map(verification -> {
            var account = accountRepository.findById(verification.accountId())
                    .orElseThrow(() -> DomainException.notFound("La cuenta de la verificación no existe."));
            return new VerificationDtos.AdminVerificationResponse(
                    verification.id(), account.id(), account.email(), account.nombreCompleto(),
                    verification.state().name(), verification.provider(), verification.submittedAt(),
                    verification.isOpen());
        }).toList();
    }

    @Transactional(readOnly = true)
    public AdminDtos.PageResponse<AdminDtos.ReportResponse> reports(ReportStatus status, int page, int size) {
        int safeSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        int safePage = Math.max(0, page);
        List<AdminDtos.ReportResponse> items = reportRepository.findByStatus(status, safePage, safeSize).stream()
                .map(AdminModerationService::toReportResponse)
                .toList();
        long total = reportRepository.count(status);
        return new AdminDtos.PageResponse<>(items, total, safePage, safeSize);
    }

    /** Resuelve una denuncia como REVISADO o DESCARTADO. No se puede reabrir. */
    @Transactional
    public AdminDtos.ReportResponse resolveReport(UUID adminId, UUID reportId, AdminDtos.ResolveReportRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> DomainException.notFound("La denuncia no existe."));
        if (!report.isOpen()) {
            throw DomainException.businessRule("Esta denuncia ya fue resuelta.");
        }
        ReportStatus decision = parseDecision(request.decision());
        Instant now = clock.instant();
        String notes = request.notes() == null ? null : request.notes().trim();
        Report resolved = reportRepository.save(report.resolved(decision, adminId, notes, now));
        securityEventRepository.save(SecurityEvent.of(report.targetId(), SecurityEventType.REPORTE_RESUELTO,
                "Denuncia " + reportId + " resuelta como " + decision.name() + " por " + adminId, now));
        return toReportResponse(resolved);
    }

    private static VerificationState parseState(String raw) {
        try {
            return VerificationState.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw DomainException.validation(
                    "Estado no válido. Valores permitidos: PENDIENTE, REVISION_HUMANA, VERIFICADA, RECHAZADA.");
        }
    }

    private static ReportStatus parseDecision(String raw) {
        if (raw == null || raw.isBlank()) {
            throw DomainException.validation("Debes indicar la decisión.");
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if (!normalized.equals("REVISADO") && !normalized.equals("DESCARTADO")) {
            throw DomainException.validation("La decisión debe ser REVISADO o DESCARTADO.");
        }
        return ReportStatus.valueOf(normalized);
    }

    private static VerificationDtos.VerificationResponse toVerificationResponse(Verification verification) {
        return new VerificationDtos.VerificationResponse(
                verification.id(),
                verification.state().name(),
                verification.provider(),
                verification.submittedAt(),
                verification.resolvedAt(),
                verification.notes(),
                verification.isOpen());
    }

    private static AdminDtos.ReportResponse toReportResponse(Report report) {
        return new AdminDtos.ReportResponse(
                report.id(),
                report.reporterId(),
                report.targetType().name(),
                report.targetId(),
                report.reason(),
                report.status().name(),
                report.reviewerId(),
                report.resolution(),
                report.createdAt(),
                report.resolvedAt());
    }
}
