package com.tinkisqa.iam.application.service;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tinkisqa.iam.application.dto.ProfileDtos;
import com.tinkisqa.iam.domain.exception.DomainException;
import com.tinkisqa.iam.domain.model.Account;
import com.tinkisqa.iam.domain.model.GenderIdentity;
import com.tinkisqa.iam.domain.model.Permission;
import com.tinkisqa.iam.domain.port.AccountRepository;
import com.tinkisqa.iam.domain.service.AccessRules;
import com.tinkisqa.iam.domain.service.AccountRules;
import com.tinkisqa.iam.domain.service.PermissionRules;

/** Casos de uso de perfil, permisos y estado de acceso. */
@Service
public class ProfileService {

    private final AccountRepository accountRepository;
    private final AdmissionService admissionService;
    private final java.time.Clock clock;

    public ProfileService(AccountRepository accountRepository, AdmissionService admissionService,
            java.time.Clock clock) {
        this.accountRepository = accountRepository;
        this.admissionService = admissionService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ProfileDtos.ProfileResponse getMe(UUID accountId) {
        return toResponse(load(accountId));
    }

    @Transactional
    public ProfileDtos.ProfileResponse updateProfile(UUID accountId, ProfileDtos.UpdateProfileRequest request) {
        Account account = load(accountId);
        Instant now = clock.instant();

        String nombre = account.nombreCompleto();
        if (request.nombreCompleto() != null && !request.nombreCompleto().isBlank()) {
            if (!AccountRules.isNombreCompletoValid(request.nombreCompleto())) {
                throw DomainException.validation("Ingresa nombres y apellidos (mínimo 3 caracteres cada uno).");
            }
            nombre = request.nombreCompleto().trim();
        }

        String bio = request.bio() == null ? account.bio() : request.bio().trim();
        if (bio != null && bio.length() > AccountRules.BIO_MAX_LENGTH) {
            throw DomainException.validation("La descripción no puede superar los 280 caracteres.");
        }

        GenderIdentity genero = parseGenero(request.genero(), account.genero());
        Set<String> intereses = request.intereses() == null
                ? account.intereses()
                : Account.normalizeIntereses(new LinkedHashSet<>(request.intereses()));
        String avatar = request.avatarUri() == null ? account.avatarUri() : request.avatarUri().trim();

        Account updated = account.withProfile(
                nombre, bio, genero, request.generoPersonalizado(), intereses, avatar, now);
        updated = admissionService.recompute(updated, now);
        return toResponse(accountRepository.save(updated));
    }

    @Transactional(readOnly = true)
    public ProfileDtos.PermissionsResponse permissions(UUID accountId) {
        return new ProfileDtos.PermissionsResponse(toResponse(load(accountId)).permissions());
    }

    @Transactional(readOnly = true)
    public ProfileDtos.AccessResponse access(UUID accountId) {
        Account account = load(accountId);
        AccessRules.AdmissionEvaluation evaluation = admissionService.evaluate(account);
        return accessResponse(account, evaluation);
    }

    @Transactional
    public ProfileDtos.AccessResponse acceptNorms(UUID accountId) {
        Account account = load(accountId);
        Instant now = clock.instant();
        Account accepted = admissionService.recompute(account.withNormasAceptadas(true, now), now);
        Account saved = accountRepository.save(accepted);
        return accessResponse(saved, admissionService.evaluate(saved));
    }

    private Account load(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> DomainException.notFound("Cuenta no encontrada."));
    }

    private ProfileDtos.ProfileResponse toResponse(Account account) {
        Set<Permission> permissions = PermissionRules.permissionsFor(account.identityStatus(), account.accessStatus());
        return new ProfileDtos.ProfileResponse(
                account.id(),
                account.email(),
                account.nombreCompleto(),
                account.bio(),
                account.genero() == null ? null : account.genero().name(),
                account.generoPersonalizado(),
                List.copyOf(account.intereses()),
                account.avatarUri(),
                account.accountStatus().name(),
                account.identityStatus().name(),
                account.accessStatus().name(),
                List.of(account.role().name()),
                account.emailVerified(),
                account.normasAceptadas(),
                permissions.stream().map(Enum::name).sorted().toList());
    }

    private ProfileDtos.AccessResponse accessResponse(Account account, AccessRules.AdmissionEvaluation evaluation) {
        List<ProfileDtos.AdmissionCriterionDto> criteria = evaluation.criteria().stream()
                .map(c -> new ProfileDtos.AdmissionCriterionDto(c.key(), c.label(), c.satisfied()))
                .toList();
        List<ProfileDtos.NormDto> norms = AccessRules.COMMUNITY_NORMS.stream()
                .map(n -> new ProfileDtos.NormDto(n.title(), n.description()))
                .toList();
        return new ProfileDtos.AccessResponse(
                account.accessStatus().name(), evaluation.admitted(), criteria, norms);
    }

    private static GenderIdentity parseGenero(String raw, GenderIdentity current) {
        if (raw == null || raw.isBlank()) {
            return current;
        }
        try {
            return GenderIdentity.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw DomainException.validation("La identidad de género indicada no es válida.");
        }
    }
}
