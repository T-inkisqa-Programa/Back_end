package com.tinkisqa.iam.application.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tinkisqa.iam.application.dto.SecurityDtos;
import com.tinkisqa.iam.domain.exception.DomainException;
import com.tinkisqa.iam.domain.model.Report;
import com.tinkisqa.iam.domain.model.ReportTargetType;
import com.tinkisqa.iam.domain.model.SecurityEvent;
import com.tinkisqa.iam.domain.model.SecurityEventType;
import com.tinkisqa.iam.domain.port.AccountRepository;
import com.tinkisqa.iam.domain.port.ReportRepository;
import com.tinkisqa.iam.domain.port.SecurityEventRepository;

/**
 * Bitácora de seguridad, denuncias y bloqueos.
 *
 * <p>Las denuncias contra usuarias se persisten en su propia tabla para poder
 * gestionarlas desde el panel de administración; además se registra un evento
 * de auditoría.</p>
 */
@Service
public class SecurityService {

    private static final int DEFAULT_LIMIT = 50;

    private final SecurityEventRepository securityEventRepository;
    private final ReportRepository reportRepository;
    private final AccountRepository accountRepository;
    private final java.time.Clock clock;

    public SecurityService(
            SecurityEventRepository securityEventRepository,
            ReportRepository reportRepository,
            AccountRepository accountRepository,
            java.time.Clock clock) {
        this.securityEventRepository = securityEventRepository;
        this.reportRepository = reportRepository;
        this.accountRepository = accountRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<SecurityDtos.SecurityEventResponse> events(UUID accountId) {
        return securityEventRepository.findRecentByAccount(accountId, DEFAULT_LIMIT).stream()
                .map(SecurityService::toResponse)
                .toList();
    }

    @Transactional
    public void report(UUID reporterId, SecurityDtos.ReportRequest request) {
        UUID targetId = parseTarget(reporterId, request.targetUserId());
        accountRepository.findById(targetId)
                .orElseThrow(() -> DomainException.notFound("La usuaria reportada no existe."));

        Instant now = clock.instant();
        String reason = request.reason().trim();
        reportRepository.save(Report.opened(reporterId, ReportTargetType.USUARIO, targetId, reason, now));
        securityEventRepository.save(SecurityEvent.of(reporterId, SecurityEventType.REPORTE_ENVIADO,
                "Reporte contra " + targetId + ": " + reason, now));
    }

    @Transactional
    public void block(UUID accountId, SecurityDtos.BlockRequest request) {
        UUID targetId = parseTarget(accountId, request.userId());
        securityEventRepository.save(SecurityEvent.of(accountId, SecurityEventType.USUARIO_BLOQUEADO,
                "Usuario bloqueado: " + targetId, clock.instant()));
    }

    private static UUID parseTarget(UUID accountId, String raw) {
        if (raw == null || raw.isBlank()) {
            throw DomainException.validation("Debes indicar el usuario objetivo.");
        }
        UUID target;
        try {
            target = UUID.fromString(raw.trim());
        } catch (IllegalArgumentException ex) {
            throw DomainException.validation("El identificador de la usuaria no es válido.");
        }
        if (target.equals(accountId)) {
            throw DomainException.validation("No puedes reportarte ni bloquearte a ti misma.");
        }
        return target;
    }

    private static SecurityDtos.SecurityEventResponse toResponse(SecurityEvent event) {
        return new SecurityDtos.SecurityEventResponse(
                event.id(), event.type().name(), event.detail(), event.createdAt());
    }
}
