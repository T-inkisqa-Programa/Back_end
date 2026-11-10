package com.tinkisqa.social.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.tinkisqa.social.domain.model.Post;
import com.tinkisqa.social.domain.model.PostFilter;
import com.tinkisqa.social.domain.model.PostView;

/** Puerto de salida: publicaciones (con sus contadores ya resueltos). */
public interface PostRepository {

    Post save(Post post);

    Optional<Post> findById(UUID id);

    List<PostView> search(PostFilter filter, UUID viewerId, int page, int size);

    long count(PostFilter filter);

    void deleteById(UUID id);
}
