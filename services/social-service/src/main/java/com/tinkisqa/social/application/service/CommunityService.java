package com.tinkisqa.social.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tinkisqa.social.application.dto.CommunityDtos;
import com.tinkisqa.social.application.dto.PageResponse;
import com.tinkisqa.social.domain.exception.DomainException;
import com.tinkisqa.social.domain.model.Community;
import com.tinkisqa.social.domain.model.CommunityPrivacy;
import com.tinkisqa.social.domain.model.JoinRequest;
import com.tinkisqa.social.domain.port.CommunityMembershipRepository;
import com.tinkisqa.social.domain.port.CommunityRepository;
import com.tinkisqa.social.domain.port.JoinRequestRepository;

/**
 * Casos de uso de comunidades y solicitudes de ingreso.
 *
 * <p>Regla de moderación: solo la dueña de la comunidad, o la administración
 * global (ROLE_ADMIN), pueden editarla, eliminarla o resolver solicitudes. Las
 * comunidades de plataforma (sin dueña) las modera únicamente la administración
 * global.</p>
 */
@Service
@Transactional
public class CommunityService {

    private static final int MAX_PAGE_SIZE = 50;

    private final CommunityRepository communities;
    private final CommunityMembershipRepository memberships;
    private final JoinRequestRepository joinRequests;
    private final Clock clock;

    public CommunityService(
            CommunityRepository communities,
            CommunityMembershipRepository memberships,
            JoinRequestRepository joinRequests,
            Clock clock) {
        this.communities = communities;
        this.memberships = memberships;
        this.joinRequests = joinRequests;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<CommunityDtos.CommunityResponse> list(
            String query, UUID ownerId, UUID viewerId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        List<Community> found = communities.search(query, ownerId, safePage, safeSize);
        long total = communities.count(query, ownerId);

        List<UUID> ids = found.stream().map(Community::id).toList();
        Map<UUID, Long> counts = memberships.countByCommunities(ids);
        Set<UUID> mine = memberships.communityIdsOf(viewerId);
        Set<UUID> pending = joinRequests.pendingCommunityIdsOf(viewerId);

        List<CommunityDtos.CommunityResponse> items = found.stream()
                .map(community -> toResponse(
                        community,
                        counts.getOrDefault(community.id(), 0L),
                        mine.contains(community.id()),
                        pending.contains(community.id()),
                        community.isOwnedBy(viewerId)))
                .toList();

        return new PageResponse<>(items, total, safePage, safeSize);
    }

    @Transactional(readOnly = true)
    public CommunityDtos.CommunityResponse get(UUID id, UUID viewerId) {
        Community community = require(id);
        return toResponse(
                community,
                memberships.countByCommunity(id),
                memberships.exists(id, viewerId),
                hasPendingRequest(id, viewerId),
                community.isOwnedBy(viewerId));
    }

    @Transactional(readOnly = true)
    public List<CommunityDtos.JoinRequestResponse> pendingRequests(UUID communityId, UUID accountId, boolean admin) {
        requireModerator(require(communityId), accountId, admin);
        return joinRequests.findPendingByCommunity(communityId).stream().map(CommunityService::toResponse).toList();
    }

    public CommunityDtos.CommunityResponse create(CommunityDtos.CreateCommunityRequest request, UUID ownerId) {
        Instant now = clock.instant();
        Community community = Community.created(
                UUID.randomUUID(),
                requireName(request.name()),
                request.description() == null ? "" : request.description().trim(),
                trimToNull(request.image()),
                trimToNull(request.category()),
                privacyOrDefault(request.privacy()),
                ownerId,
                now);

        Community saved = communities.save(community);
        // Quien crea la comunidad pasa a ser su primer integrante.
        memberships.add(saved.id(), ownerId, now);
        return toResponse(saved, 1, true, false, true);
    }

    public CommunityDtos.CommunityResponse update(
            UUID id, CommunityDtos.UpdateCommunityRequest request, UUID accountId, boolean admin) {
        Community community = require(id);
        requireModerator(community, accountId, admin);
        Instant now = clock.instant();

        Community updated = community.withDetails(
                request.name() == null ? community.name() : requireName(request.name()),
                request.description() == null ? community.description() : request.description().trim(),
                request.image() == null ? community.imageUrl() : trimToNull(request.image()),
                request.category() == null ? community.category() : trimToNull(request.category()),
                request.privacy() == null ? community.privacy() : privacyOrDefault(request.privacy()),
                now);

        Community saved = communities.save(updated);
        return toResponse(
                saved,
                memberships.countByCommunity(id),
                memberships.exists(id, accountId),
                hasPendingRequest(id, accountId),
                saved.isOwnedBy(accountId));
    }

    public void delete(UUID id, UUID accountId, boolean admin) {
        requireModerator(require(id), accountId, admin);
        // Las membresías y solicitudes caen por ON DELETE CASCADE; las
        // publicaciones de la comunidad quedan sin comunidad (SET NULL).
        communities.deleteById(id);
    }

    public CommunityDtos.JoinRequestResponse requestJoin(UUID communityId, UUID accountId, String requesterName) {
        Community community = require(communityId);
        if (community.isOwnedBy(accountId)) {
            throw DomainException.businessRule("Ya eres la dueña de esta comunidad.");
        }
        if (memberships.exists(communityId, accountId)) {
            throw DomainException.conflict("Ya perteneces a esta comunidad.");
        }

        Instant now = clock.instant();
        JoinRequest request = joinRequests.findByCommunityAndAccount(communityId, accountId)
                .map(existing -> existing.isPending() ? existing : existing.reopened(now))
                .orElseGet(() -> JoinRequest.opened(
                        UUID.randomUUID(), communityId, accountId, requesterName, now));

        return toResponse(joinRequests.save(request));
    }

    public void cancelJoinRequest(UUID communityId, UUID accountId) {
        require(communityId);
        JoinRequest request = joinRequests.findByCommunityAndAccount(communityId, accountId)
                .filter(JoinRequest::isPending)
                .orElseThrow(() -> DomainException.notFound("No tienes una solicitud pendiente en esta comunidad."));
        joinRequests.save(request.rejected(accountId, clock.instant()));
    }

    public CommunityDtos.JoinRequestResponse resolve(
            UUID communityId, UUID requestId, boolean approve, UUID accountId, boolean admin) {
        requireModerator(require(communityId), accountId, admin);

        JoinRequest request = joinRequests.findById(requestId)
                .filter(found -> found.communityId().equals(communityId))
                .orElseThrow(() -> DomainException.notFound("Solicitud no encontrada."));
        if (!request.isPending()) {
            throw DomainException.conflict("La solicitud ya fue resuelta.");
        }

        Instant now = clock.instant();
        JoinRequest resolved = approve ? request.approved(accountId, now) : request.rejected(accountId, now);
        if (approve) {
            memberships.add(request.communityId(), request.accountId(), now);
        }
        return toResponse(joinRequests.save(resolved));
    }

    public void leave(UUID communityId, UUID accountId) {
        Community community = require(communityId);
        if (community.isOwnedBy(accountId)) {
            throw DomainException.businessRule("La dueña no puede salir de su propia comunidad.");
        }
        if (!memberships.exists(communityId, accountId)) {
            throw DomainException.conflict("No perteneces a esta comunidad.");
        }
        memberships.remove(communityId, accountId);
    }

    /**
     * Comprueba que la cuenta pueda publicar en la comunidad indicada. Una
     * comunidad nula (muro general) siempre se permite.
     */
    @Transactional(readOnly = true)
    public void requirePublisher(UUID communityId, UUID accountId, boolean admin) {
        if (communityId == null) {
            return;
        }
        Community community = require(communityId);
        boolean allowed = admin || community.isOwnedBy(accountId) || memberships.exists(communityId, accountId);
        if (!allowed) {
            throw DomainException.forbidden("Debes pertenecer a la comunidad para publicar en ella.");
        }
    }

    private Community require(UUID id) {
        return communities.findById(id).orElseThrow(() -> DomainException.notFound("Comunidad no encontrada."));
    }

    private void requireModerator(Community community, UUID accountId, boolean admin) {
        if (!admin && !community.isOwnedBy(accountId)) {
            throw DomainException.forbidden("No puedes moderar esta comunidad.");
        }
    }

    private boolean hasPendingRequest(UUID communityId, UUID accountId) {
        return joinRequests.findByCommunityAndAccount(communityId, accountId)
                .filter(JoinRequest::isPending)
                .isPresent();
    }

    private static CommunityPrivacy privacyOrDefault(String privacy) {
        return privacy == null || privacy.isBlank()
                ? CommunityPrivacy.PUBLICA
                : CommunityPrivacy.valueOf(privacy);
    }

    private static String requireName(String name) {
        String trimmed = name.trim();
        if (trimmed.length() < 3) {
            throw DomainException.validation("El nombre de la comunidad debe tener al menos 3 caracteres.");
        }
        return trimmed;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static CommunityDtos.CommunityResponse toResponse(
            Community community, long memberCount, boolean member, boolean pendingRequest, boolean owner) {
        return new CommunityDtos.CommunityResponse(
                community.id(),
                community.name(),
                community.description(),
                community.imageUrl(),
                community.category(),
                community.privacy().name(),
                community.ownerId(),
                community.ownerId() != null,
                owner,
                member,
                pendingRequest,
                memberCount,
                community.createdAt(),
                community.updatedAt());
    }

    private static CommunityDtos.JoinRequestResponse toResponse(JoinRequest request) {
        return new CommunityDtos.JoinRequestResponse(
                request.id(),
                request.communityId(),
                request.accountId(),
                request.requesterName(),
                request.status().name(),
                request.createdAt(),
                request.resolvedAt());
    }
}
