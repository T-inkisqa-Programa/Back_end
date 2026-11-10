package com.tinkisqa.social.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTOs del caso de uso de publicaciones, comentarios e interacciones.
 *
 * <p>La respuesta incluye datos resueltos para quien consulta ({@code liked},
 * {@code saved}, {@code mine}) y las instantáneas de autora, de modo que el
 * frontend no necesita consultar el IAM para pintar el muro.</p>
 */
public final class PostDtos {

    private PostDtos() {
    }

    public record PostResponse(
            UUID id,
            UUID authorId,
            String author,
            String avatar,
            UUID communityId,
            String tag,
            String content,
            boolean quote,
            String image,
            String audioUrl,
            String audioName,
            String pdfUrl,
            String pdfName,
            String feeling,
            String activity,
            String location,
            java.util.List<String> people,
            String privacy,
            long likes,
            long comments,
            long saves,
            boolean liked,
            boolean saved,
            boolean mine,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record PublishPostRequest(
            @NotBlank @Size(max = 5000) String content,
            @Size(max = 60) String tag,
            UUID communityId,
            boolean quote,
            @Size(max = 2048) String image,
            @Size(max = 2048) String audio,
            @Size(max = 200) String audioName,
            @Size(max = 2048) String pdf,
            @Size(max = 200) String pdfName,
            @Size(max = 60) String feeling,
            @Size(max = 60) String activity,
            @Size(max = 120) String location,
            @Size(max = 30) java.util.List<@Size(max = 140) String> people,
            @Pattern(regexp = "PUBLICO|PRIVADO", message = "La privacidad debe ser PUBLICO o PRIVADO.")
            String privacy) {
    }

    public record UpdatePostRequest(
            @NotBlank @Size(max = 5000) String content,
            @Size(max = 60) String tag,
            @Size(max = 2048) String image,
            @Size(max = 2048) String audio,
            @Size(max = 200) String audioName,
            @Size(max = 2048) String pdf,
            @Size(max = 200) String pdfName,
            @Size(max = 60) String feeling,
            @Size(max = 60) String activity,
            @Size(max = 120) String location,
            @Size(max = 30) java.util.List<@Size(max = 140) String> people,
            @Pattern(regexp = "PUBLICO|PRIVADO", message = "La privacidad debe ser PUBLICO o PRIVADO.")
            String privacy) {
    }

    public record CommentResponse(
            UUID id,
            UUID postId,
            UUID authorId,
            String author,
            String avatar,
            String text,
            boolean mine,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record AddCommentRequest(@NotBlank @Size(max = 2000) String text) {
    }
}
