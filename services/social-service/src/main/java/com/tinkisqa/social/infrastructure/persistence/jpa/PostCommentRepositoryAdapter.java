package com.tinkisqa.social.infrastructure.persistence.jpa;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.tinkisqa.social.domain.model.PostComment;
import com.tinkisqa.social.domain.port.PostCommentRepository;

/** Adaptador JPA del puerto {@link PostCommentRepository}. */
@Component
public class PostCommentRepositoryAdapter implements PostCommentRepository {

    private final PostCommentJpaRepository repository;

    public PostCommentRepositoryAdapter(PostCommentJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public PostComment save(PostComment comment) {
        return toDomain(repository.save(toEntity(comment)));
    }

    @Override
    public Optional<PostComment> findById(UUID id) {
        return repository.findById(id).map(PostCommentRepositoryAdapter::toDomain);
    }

    @Override
    public List<PostComment> findByPost(UUID postId) {
        return repository.findByPostIdOrderByCreatedAtAsc(postId).stream()
                .map(PostCommentRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public long countByPost(UUID postId) {
        return repository.countByPostId(postId);
    }

    @Override
    public Map<UUID, Long> countByPosts(Collection<UUID> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Long> counts = new HashMap<>();
        for (Object[] row : repository.countGroupedByPost(List.copyOf(postIds))) {
            counts.put((UUID) row[0], ((Number) row[1]).longValue());
        }
        return counts;
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private static PostCommentJpaEntity toEntity(PostComment comment) {
        PostCommentJpaEntity entity = new PostCommentJpaEntity();
        entity.setId(comment.id());
        entity.setPostId(comment.postId());
        entity.setAuthorId(comment.authorId());
        entity.setAuthorName(comment.authorName());
        entity.setAuthorAvatar(comment.authorAvatar());
        entity.setText(comment.text());
        entity.setCreatedAt(comment.createdAt());
        entity.setUpdatedAt(comment.updatedAt());
        return entity;
    }

    private static PostComment toDomain(PostCommentJpaEntity entity) {
        return new PostComment(
                entity.getId(),
                entity.getPostId(),
                entity.getAuthorId(),
                entity.getAuthorName(),
                entity.getAuthorAvatar(),
                entity.getText(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
