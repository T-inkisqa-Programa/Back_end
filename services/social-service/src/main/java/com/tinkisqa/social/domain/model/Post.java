package com.tinkisqa.social.domain.model;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Publicación del muro. Guarda una instantánea del nombre y avatar de la autora
 * (no se consulta el IAM para pintar el muro).
 *
 * <p>{@code communityId} nulo = publicación general (muro principal).</p>
 */
public record Post(
        UUID id,
        UUID authorId,
        String authorName,
        String authorAvatar,
        UUID communityId,
        String tag,
        String content,
        boolean quote,
        String imageUrl,
        String audioUrl,
        String audioName,
        String pdfUrl,
        String pdfName,
        String feeling,
        String activity,
        String location,
        Set<String> people,
        PostPrivacy privacy,
        Instant createdAt,
        Instant updatedAt) {

    public Post {
        people = people == null ? new LinkedHashSet<>() : new LinkedHashSet<>(people);
    }

    public static Post published(
            UUID id,
            UUID authorId,
            String authorName,
            String authorAvatar,
            UUID communityId,
            String tag,
            String content,
            boolean quote,
            String imageUrl,
            String audioUrl,
            String audioName,
            String pdfUrl,
            String pdfName,
            String feeling,
            String activity,
            String location,
            Set<String> people,
            PostPrivacy privacy,
            Instant now) {
        return new Post(id, authorId, authorName, authorAvatar, communityId, tag, content, quote,
                imageUrl, audioUrl, audioName, pdfUrl, pdfName, feeling, activity, location, people, privacy, now,
                now);
    }

    public Post withContent(
            String tag,
            String content,
            String imageUrl,
            String audioUrl,
            String audioName,
            String pdfUrl,
            String pdfName,
            String feeling,
            String activity,
            String location,
            Set<String> people,
            PostPrivacy privacy,
            Instant now) {
        return new Post(id, authorId, authorName, authorAvatar, communityId, tag, content, quote,
                imageUrl, audioUrl, audioName, pdfUrl, pdfName, feeling, activity, location, people, privacy,
                createdAt, now);
    }

    public boolean isAuthoredBy(UUID accountId) {
        return authorId.equals(accountId);
    }
}
