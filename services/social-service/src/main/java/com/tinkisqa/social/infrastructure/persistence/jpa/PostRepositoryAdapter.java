package com.tinkisqa.social.infrastructure.persistence.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.tinkisqa.social.domain.model.InteractionType;
import com.tinkisqa.social.domain.model.Post;
import com.tinkisqa.social.domain.model.PostFilter;
import com.tinkisqa.social.domain.model.PostView;
import com.tinkisqa.social.domain.port.PostCommentRepository;
import com.tinkisqa.social.domain.port.PostInteractionRepository;
import com.tinkisqa.social.domain.port.PostRepository;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

/**
 * Adaptador JPA del puerto {@link PostRepository}.
 *
 * <p>Los contadores y las marcas por usuaria se resuelven con consultas
 * agrupadas por página (una por tipo), evitando el patrón N+1.</p>
 */
@Component
public class PostRepositoryAdapter implements PostRepository {

    private final PostJpaRepository posts;
    private final PostInteractionRepository interactions;
    private final PostCommentRepository comments;

    public PostRepositoryAdapter(
            PostJpaRepository posts,
            PostInteractionRepository interactions,
            PostCommentRepository comments) {
        this.posts = posts;
        this.interactions = interactions;
        this.comments = comments;
    }

    @Override
    public Post save(Post post) {
        return toDomain(posts.save(toEntity(post)));
    }

    @Override
    public Optional<Post> findById(UUID id) {
        return posts.findById(id).map(PostRepositoryAdapter::toDomain);
    }

    @Override
    public List<PostView> search(PostFilter filter, UUID viewerId, int page, int size) {
        List<PostJpaEntity> found = posts
                .findAll(filter(filter), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent();
        if (found.isEmpty()) {
            return List.of();
        }

        List<UUID> ids = found.stream().map(PostJpaEntity::getId).toList();
        Map<UUID, Long> likes = interactions.countByPosts(ids, InteractionType.LIKE);
        Map<UUID, Long> saves = interactions.countByPosts(ids, InteractionType.SAVE);
        Map<UUID, Long> commentCounts = comments.countByPosts(ids);

        Set<UUID> liked = viewerId == null
                ? Set.of()
                : interactions.postIdsMarkedBy(viewerId, InteractionType.LIKE, ids);
        Set<UUID> saved = viewerId == null
                ? Set.of()
                : interactions.postIdsMarkedBy(viewerId, InteractionType.SAVE, ids);

        return found.stream()
                .map(entity -> new PostView(
                        toDomain(entity),
                        likes.getOrDefault(entity.getId(), 0L),
                        commentCounts.getOrDefault(entity.getId(), 0L),
                        saves.getOrDefault(entity.getId(), 0L),
                        liked.contains(entity.getId()),
                        saved.contains(entity.getId())))
                .toList();
    }

    @Override
    public long count(PostFilter filter) {
        return posts.count(filter(filter));
    }

    @Override
    public void deleteById(UUID id) {
        posts.deleteById(id);
    }

    private static Specification<PostJpaEntity> filter(PostFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.communityId() != null) {
                predicates.add(cb.equal(root.get("communityId"), filter.communityId()));
            }
            if (filter.authorId() != null) {
                predicates.add(cb.equal(root.get("authorId"), filter.authorId()));
            }
            if (filter.query() != null && !filter.query().isBlank()) {
                String like = "%" + filter.query().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.like(cb.lower(root.get("content")), like));
            }
            if (filter.savedBy() != null) {
                Subquery<UUID> subquery = query.subquery(UUID.class);
                Root<PostInteractionJpaEntity> interaction = subquery.from(PostInteractionJpaEntity.class);
                subquery.select(interaction.get("postId"))
                        .where(cb.equal(interaction.get("postId"), root.get("id")),
                                cb.equal(interaction.get("accountId"), filter.savedBy()),
                                cb.equal(interaction.get("type"), InteractionType.SAVE));
                predicates.add(cb.exists(subquery));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static PostJpaEntity toEntity(Post post) {
        PostJpaEntity entity = new PostJpaEntity();
        entity.setId(post.id());
        entity.setAuthorId(post.authorId());
        entity.setAuthorName(post.authorName());
        entity.setAuthorAvatar(post.authorAvatar());
        entity.setCommunityId(post.communityId());
        entity.setTag(post.tag());
        entity.setContent(post.content());
        entity.setQuote(post.quote());
        entity.setImageUrl(post.imageUrl());
        entity.setAudioUrl(post.audioUrl());
        entity.setAudioName(post.audioName());
        entity.setPdfUrl(post.pdfUrl());
        entity.setPdfName(post.pdfName());
        entity.setFeeling(post.feeling());
        entity.setActivity(post.activity());
        entity.setLocation(post.location());
        entity.setPeople(post.people());
        entity.setPrivacy(post.privacy());
        entity.setCreatedAt(post.createdAt());
        entity.setUpdatedAt(post.updatedAt());
        return entity;
    }

    private static Post toDomain(PostJpaEntity entity) {
        return new Post(
                entity.getId(),
                entity.getAuthorId(),
                entity.getAuthorName(),
                entity.getAuthorAvatar(),
                entity.getCommunityId(),
                entity.getTag(),
                entity.getContent(),
                entity.isQuote(),
                entity.getImageUrl(),
                entity.getAudioUrl(),
                entity.getAudioName(),
                entity.getPdfUrl(),
                entity.getPdfName(),
                entity.getFeeling(),
                entity.getActivity(),
                entity.getLocation(),
                entity.getPeople() == null ? Set.of() : Set.copyOf(entity.getPeople()),
                entity.getPrivacy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
