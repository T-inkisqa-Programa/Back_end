package com.tinkisqa.social.presentation.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tinkisqa.social.application.service.PostService;
import com.tinkisqa.social.presentation.security.CurrentUser;

/**
 * Operaciones sobre un comentario concreto, fuera del contexto de su
 * publicación.
 *
 * <p>Solo la autora del comentario o {@code ROLE_ADMIN} pueden eliminarlo.</p>
 */
@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final PostService postService;
    private final CurrentUser currentUser;

    public CommentController(PostService postService, CurrentUser currentUser) {
        this.postService = postService;
        this.currentUser = currentUser;
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('PERM_COMENTAR')")
    public void delete(@PathVariable UUID commentId) {
        postService.deleteComment(commentId, currentUser.requireAccountId(), currentUser.isAdmin());
    }
}
