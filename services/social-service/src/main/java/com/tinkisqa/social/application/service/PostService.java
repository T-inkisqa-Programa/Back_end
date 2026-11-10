package com.tinkisqa.social.application.service;

import java.time.Clock;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tinkisqa.social.application.dto.PageResponse;
import com.tinkisqa.social.application.dto.PostDtos;
import com.tinkisqa.social.domain.exception.DomainException;
import com.tinkisqa.social.domain.model.InteractionType;
import com.tinkisqa.social.domain.model.Post;
import com.tinkisqa.social.domain.model.PostComment;
import com.tinkisqa.social.domain.model.PostFilter;
import com.tinkisqa.social.domain.model.PostPrivacy;
import com.tinkisqa.social.domain.model.PostView;
import com.tinkisqa.social.domain.port.PostCommentRepository;
import com.tinkisqa.social.domain.port.PostInteractionRepository;
import com.tinkisqa.social.domain.port.PostRepository;

/**
 * Casos de uso de publicaciones, comentarios e interacciones.
 *
 * <p>Reglas: solo la autora (o ROLE_ADMIN) edita o elimina su contenido; para
 * publicar dentro de una comunidad hay que pertenecer a ella.</p>
 */
@Service
@Transactional
public class PostService {

    private static final int MAX_PAGE_SIZE = 50;

    private final PostRepository posts;
    private final PostCommentRepository comments;
    private final PostInteractionRepository interactions;
    private final CommunityService communityService;
    private final Clock clock;

    public PostService(
            PostRepository posts,
            PostCommentRepository comments,
            PostInteractionRepository interactions,
            CommunityService communityService,
            Clock clock) {
        this.posts = posts;
        this.comments = comments;
        this.interactions = interactions;
        this.communityService = communityService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<PostDtos.PostResponse> list(PostFilter filter, UUID viewerId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        List<PostView> found = posts.search(filter, viewerId, safePage, safeSize);
        long total = posts.count(filter);

        return new PageResponse<>(
                found.stream().map(view -> toResponse(view, viewerId)).toList(), total, safePage, safeSize);
    }

    @Transactional(readOnly = true)
    public PostDtos.PostResponse get(UUID id, UUID viewerId) {
        return toResponse(loadView(id, viewerId), viewerId);
    }

    public PostDtos.PostResponse publish(
            PostDtos.PublishPostRequest request,
            UUID authorId,
            String authorName,
            String authorAvatar,
            boolean admin) {
        communityService.requirePublisher(request.communityId(), authorId, admin);

        Post post = Post.published(
                UUID.randomUUID(),
                authorId,
                authorName,
                authorAvatar,
                request.communityId(),
                trimToNull(request.tag()),
                request.content().trim(),
                request.quote(),
                trimToNull(request.image()),
                trimToNull(request.audio()),
                trimToNull(request.audioName()),
                trimToNull(request.pdf()),
                trimToNull(request.pdfName()),
                trimToNull(request.feeling()),
                trimToNull(request.activity()),
                trimToNull(request.location()),
                cleanPeople(request.people()),
                privacyOrDefault(request.privacy()),
                clock.instant());

        Post saved = posts.save(post);
        return toResponse(new PostView(saved, 0, 0, 0, false, false), authorId);
    }

    public PostDtos.PostResponse update(
            UUID id, PostDtos.UpdatePostRequest request, UUID accountId, boolean admin) {
        Post post = require(id);
        requireAuthor(post, accountId, admin);

        Post updated = post.withContent(
                trimToNull(request.tag()),
                request.content().trim(),
                trimToNull(request.image()),
                trimToNull(request.audio()),
                trimToNull(request.audioName()),
                trimToNull(request.pdf()),
                trimToNull(request.pdfName()),
                trimToNull(request.feeling()),
                trimToNull(request.activity()),
                trimToNull(request.location()),
                cleanPeople(request.people()),
                privacyOrDefault(request.privacy()),
                clock.instant());

        return toResponse(loadView(posts.save(updated).id(), accountId), accountId);
    }

    public void delete(UUID id, UUID accountId, boolean admin) {
        requireAuthor(require(id), accountId, admin);
        // Comentarios, personas e interacciones caen por ON DELETE CASCADE.
        posts.deleteById(id);
    }

    public PostDtos.PostResponse like(UUID id, UUID accountId) {
        require(id);
        interactions.add(id, accountId, InteractionType.LIKE, clock.instant());
        return toResponse(loadView(id, accountId), accountId);
    }

    public PostDtos.PostResponse unlike(UUID id, UUID accountId) {
        require(id);
        interactions.remove(id, accountId, InteractionType.LIKE);
        return toResponse(loadView(id, accountId), accountId);
    }

    public PostDtos.PostResponse save(UUID id, UUID accountId) {
        require(id);
        interactions.add(id, accountId, InteractionType.SAVE, clock.instant());
        return toResponse(loadView(id, accountId), accountId);
    }

    public PostDtos.PostResponse unsave(UUID id, UUID accountId) {
        require(id);
        interactions.remove(id, accountId, InteractionType.SAVE);
        return toResponse(loadView(id, accountId), accountId);
    }

    @Transactional(readOnly = true)
    public List<PostDtos.CommentResponse> comments(UUID postId, UUID viewerId) {
        require(postId);
        return comments.findByPost(postId).stream()
                .map(comment -> toResponse(comment, viewerId))
                .toList();
    }

    public PostDtos.CommentResponse comment(
            UUID postId, PostDtos.AddCommentRequest request, UUID authorId, String authorName, String authorAvatar) {
        require(postId);
        PostComment created = PostComment.added(
                UUID.randomUUID(), postId, authorId, authorName, authorAvatar, request.text().trim(), clock.instant());
        return toResponse(comments.save(created), authorId);
    }

    public void deleteComment(UUID commentId, UUID accountId, boolean admin) {
        PostComment comment = comments.findById(commentId)
                .orElseThrow(() -> DomainException.notFound("Comentario no encontrado."));
        if (!admin && !comment.isAuthoredBy(accountId)) {
            throw DomainException.forbidden("Solo la autora del comentario puede eliminarlo.");
        }
        comments.deleteById(commentId);
    }

    private PostView loadView(UUID id, UUID viewerId) {
        Post post = require(id);
        return new PostView(
                post,
                interactions.countByPost(id, InteractionType.LIKE),
                comments.countByPost(id),
                interactions.countByPost(id, InteractionType.SAVE),
                viewerId != null && interactions.exists(id, viewerId, InteractionType.LIKE),
                viewerId != null && interactions.exists(id, viewerId, InteractionType.SAVE));
    }

    private Post require(UUID id) {
        return posts.findById(id).orElseThrow(() -> DomainException.notFound("Publicación no encontrada."));
    }

    private void requireAuthor(Post post, UUID accountId, boolean admin) {
        if (!admin && !post.isAuthoredBy(accountId)) {
            throw DomainException.forbidden("Solo la autora puede modificar esta publicación.");
        }
    }

    private static Set<String> cleanPeople(List<String> people) {
        if (people == null || people.isEmpty()) {
            return Set.of();
        }
        Set<String> cleaned = new LinkedHashSet<>();
        for (String person : people) {
            String trimmed = trimToNull(people == null ? null : person);
            if (trimmed != null) {
                cleaned.add(trimmed);
            }
        }
        return cleaned;
    }

    private static PostPrivacy privacyOrDefault(String privacy) {
        return privacy == null || privacy.isBlank()
                ? PostPrivacy.PUBLICO
                : PostPrivacy.valueOf(privacy);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static PostDtos.PostResponse toResponse(PostView view, UUID viewerId) {
        Post post = view.post();
        return new PostDtos.PostResponse(
                post.id(),
                post.authorId(),
                post.authorName(),
                post.authorAvatar(),
                post.communityId(),
                post.tag(),
                post.content(),
                post.quote(),
                post.imageUrl(),
                post.audioUrl(),
                post.audioName(),
                post.pdfUrl(),
                post.pdfName(),
                post.feeling(),
                post.activity(),
                post.location(),
                List.copyOf(post.people()),
                post.privacy().name(),
                view.likeCount(),
                view.commentCount(),
                view.saveCount(),
                view.likedByViewer(),
                view.savedByViewer(),
                viewerId != null && post.isAuthoredBy(viewerId),
                post.createdAt(),
                post.updatedAt());
    }

    private static PostDtos.CommentResponse toResponse(PostComment comment, UUID viewerId) {
        return new PostDtos.CommentResponse(
                comment.id(),
                comment.postId(),
                comment.authorId(),
                comment.authorName(),
                comment.authorAvatar(),
                comment.text(),
                viewerId != null && comment.isAuthoredBy(viewerId),
                comment.createdAt(),
                comment.updatedAt());
    }
}
