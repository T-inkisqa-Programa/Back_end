package com.tinkisqa.social.presentation.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tinkisqa.social.application.dto.CommunityDtos;
import com.tinkisqa.social.application.dto.PageResponse;
import com.tinkisqa.social.application.service.CommunityService;
import com.tinkisqa.social.presentation.security.CurrentUser;

import jakarta.validation.Valid;

/**
 * Comunidades y solicitudes de ingreso.
 *
 * <p>Todas las operaciones exigen el permiso funcional {@code FOROS}; la
 * moderación (editar, eliminar, resolver solicitudes) se valida además en el
 * servicio: solo la dueña de la comunidad o ROLE_ADMIN.</p>
 */
@RestController
@RequestMapping("/api/v1/communities")
public class CommunityController {

    private final CommunityService communityService;
    private final CurrentUser currentUser;

    public CommunityController(CommunityService communityService, CurrentUser currentUser) {
        this.communityService = communityService;
        this.currentUser = currentUser;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_FOROS')")
    public PageResponse<CommunityDtos.CommunityResponse> list(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "ownerId", required = false) UUID ownerId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return communityService.list(query, ownerId, currentUser.requireAccountId(), page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_FOROS')")
    public CommunityDtos.CommunityResponse get(@PathVariable UUID id) {
        return communityService.get(id, currentUser.requireAccountId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PERM_FOROS')")
    public CommunityDtos.CommunityResponse create(
            @Valid @RequestBody CommunityDtos.CreateCommunityRequest request) {
        return communityService.create(request, currentUser.requireAccountId());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_FOROS')")
    public CommunityDtos.CommunityResponse update(
            @PathVariable UUID id, @Valid @RequestBody CommunityDtos.UpdateCommunityRequest request) {
        return communityService.update(id, request, currentUser.requireAccountId(), currentUser.isAdmin());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('PERM_FOROS')")
    public void delete(@PathVariable UUID id) {
        communityService.delete(id, currentUser.requireAccountId(), currentUser.isAdmin());
    }

    @GetMapping("/{id}/requests")
    @PreAuthorize("hasAuthority('PERM_FOROS')")
    public List<CommunityDtos.JoinRequestResponse> pendingRequests(@PathVariable UUID id) {
        return communityService.pendingRequests(id, currentUser.requireAccountId(), currentUser.isAdmin());
    }

    @PostMapping("/{id}/join")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAuthority('PERM_FOROS')")
    public CommunityDtos.JoinRequestResponse join(@PathVariable UUID id) {
        return communityService.requestJoin(id, currentUser.requireAccountId(), currentUser.displayName());
    }

    @DeleteMapping("/{id}/join")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('PERM_FOROS')")
    public void cancelJoin(@PathVariable UUID id) {
        communityService.cancelJoinRequest(id, currentUser.requireAccountId());
    }

    @PostMapping("/{id}/requests/{requestId}/approve")
    @PreAuthorize("hasAuthority('PERM_FOROS')")
    public CommunityDtos.JoinRequestResponse approve(@PathVariable UUID id, @PathVariable UUID requestId) {
        return communityService.resolve(id, requestId, true, currentUser.requireAccountId(), currentUser.isAdmin());
    }

    @PostMapping("/{id}/requests/{requestId}/reject")
    @PreAuthorize("hasAuthority('PERM_FOROS')")
    public CommunityDtos.JoinRequestResponse reject(@PathVariable UUID id, @PathVariable UUID requestId) {
        return communityService.resolve(id, requestId, false, currentUser.requireAccountId(), currentUser.isAdmin());
    }

    @DeleteMapping("/{id}/membership")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('PERM_FOROS')")
    public void leave(@PathVariable UUID id) {
        communityService.leave(id, currentUser.requireAccountId());
    }
}
