package com.tinkisqa.social.domain.model;

import java.time.Instant;
import java.util.UUID;

/** Comentario de una publicación, con la instantánea de su autora. */
public record PostComment(
        UUID id,
        UUID postId,
        UUID authorId,
        String authorName,
        String authorAvatar,
        String text,
        Instant createdAt,
        Instant updatedAt) {

    public static PostComment added(
            UUID id, UUID postId, UUID authorId, String authorName, String authorAvatar, String text, Instant now) {
        return new PostComment(id, postId, authorId, authorName, authorAvatar, text, now, now);
    }

    public PostComment withText(String text, Instant now) {
        return new PostComment(id, postId, authorId, authorName, authorAvatar, text, createdAt, now);
    }

    public boolean isAuthoredBy(UUID accountId) {
        return authorId.equals(accountId);
    }
}
