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

import com.tinkisqa.social.application.dto.PageResponse;
import com.tinkisqa.social.application.dto.PostDtos;
import com.tinkisqa.social.application.service.PostService;
import com.tinkisqa.social.domain.model.PostFilter;
import com.tinkisqa.social.presentation.security.CurrentUser;

import jakarta.validation.Valid;

/**
 * Publicaciones, comentarios e interacciones (me gusta / guardados).
 *
 * <p>Leer el muro requiere el permiso {@code FOROS}; publicar y comentar
 * requieren {@code PUBLICAR} y {@code COMENTAR} respectivamente.</p>
 */
@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;
    private final CurrentUser currentUser;

    public PostController(PostService postService, CurrentUser currentUser) {
        this.postService = postService;
        this.currentUser = currentUser;
    }

    /**
     * Muro de publicaciones.
     *
     * @param saved {@code true} devuelve solo las publicaciones guardadas por
     *              quien consulta (muro de guardados)
     * @param mine  {@code true} devuelve solo las publicaciones propias
     */
    @GetMapping
    @PreAuthorize("hasAuthority('PERM_FOROS')")
    public PageResponse<PostDtos.PostResponse> list(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "communityId", required = false) UUID communityId,
            @RequestParam(name = "authorId", required = false) UUID authorId,
            @RequestParam(name = "saved", defaultValue = "false") boolean saved,
            @RequestParam(name = "mine", defaultValue = "false") boolean mine,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        UUID viewerId = currentUser.requireAccountId();
        PostFilter filter = new PostFilter(
                query,
                communityId,
                mine ? viewerId : authorId,
                saved ? viewerId : null);
        return postService.list(filter, viewerId, page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_FOROS')")
    public PostDtos.PostResponse get(@PathVariable UUID id) {
        return postService.get(id, currentUser.requireAccountId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PERM_PUBLICAR')")
    public PostDtos.PostResponse publish(@Valid @RequestBody PostDtos.PublishPostRequest request) {
        return postService.publish(
                request,
                currentUser.requireAccountId(),
                currentUser.displayName(),
                currentUser.avatar(),
                currentUser.isAdmin());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_PUBLICAR')")
    public PostDtos.PostResponse update(
            @PathVariable UUID id, @Valid @RequestBody PostDtos.UpdatePostRequest request) {
        return postService.update(id, request, currentUser.requireAccountId(), currentUser.isAdmin());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('PERM_PUBLICAR')")
    public void delete(@PathVariable UUID id) {
        postService.delete(id, currentUser.requireAccountId(), currentUser.isAdmin());
    }

    @PostMapping("/{id}/likes")
    @PreAuthorize("hasAuthority('PERM_COMENTAR')")
    public PostDtos.PostResponse like(@PathVariable UUID id) {
        return postService.like(id, currentUser.requireAccountId());
    }

    @DeleteMapping("/{id}/likes")
    @PreAuthorize("hasAuthority('PERM_COMENTAR')")
    public PostDtos.PostResponse unlike(@PathVariable UUID id) {
        return postService.unlike(id, currentUser.requireAccountId());
    }

    @PostMapping("/{id}/saves")
    @PreAuthorize("hasAuthority('PERM_COMENTAR')")
    public PostDtos.PostResponse save(@PathVariable UUID id) {
        return postService.save(id, currentUser.requireAccountId());
    }

    @DeleteMapping("/{id}/saves")
    @PreAuthorize("hasAuthority('PERM_COMENTAR')")
    public PostDtos.PostResponse unsave(@PathVariable UUID id) {
        return postService.unsave(id, currentUser.requireAccountId());
    }

    @GetMapping("/{id}/comments")
    @PreAuthorize("hasAuthority('PERM_FOROS')")
    public List<PostDtos.CommentResponse> comments(@PathVariable UUID id) {
        return postService.comments(id, currentUser.requireAccountId());
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PERM_COMENTAR')")
    public PostDtos.CommentResponse comment(
            @PathVariable UUID id, @Valid @RequestBody PostDtos.AddCommentRequest request) {
        return postService.comment(
                id, request, currentUser.requireAccountId(), currentUser.displayName(), currentUser.avatar());
    }
}
