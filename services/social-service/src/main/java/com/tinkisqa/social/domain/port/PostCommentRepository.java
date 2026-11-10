package com.tinkisqa.social.domain.port;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.tinkisqa.social.domain.model.PostComment;

/** Puerto de salida: comentarios de publicaciones. */
public interface PostCommentRepository {

    PostComment save(PostComment comment);

    Optional<PostComment> findById(UUID id);

    List<PostComment> findByPost(UUID postId);

    long countByPost(UUID postId);

    /** Comentarios por publicación, en una sola consulta (evita N+1). */
    Map<UUID, Long> countByPosts(Collection<UUID> postIds);

    void deleteById(UUID id);
}
