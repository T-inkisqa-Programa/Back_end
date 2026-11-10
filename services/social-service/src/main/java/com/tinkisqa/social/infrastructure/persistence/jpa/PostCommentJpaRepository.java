package com.tinkisqa.social.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostCommentJpaRepository extends JpaRepository<PostCommentJpaEntity, UUID> {

    List<PostCommentJpaEntity> findByPostIdOrderByCreatedAtAsc(UUID postId);

    long countByPostId(UUID postId);

    void deleteByPostId(UUID postId);

    @Query("""
            select c.postId, count(c)
            from PostCommentJpaEntity c
            where c.postId in :postIds
            group by c.postId
            """)
    List<Object[]> countGroupedByPost(@Param("postIds") List<UUID> postIds);
}
