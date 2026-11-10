package com.tinkisqa.social.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tinkisqa.social.domain.model.InteractionType;

public interface PostInteractionJpaRepository extends JpaRepository<PostInteractionJpaEntity, UUID> {

    boolean existsByPostIdAndAccountIdAndType(UUID postId, UUID accountId, InteractionType type);

    long countByPostIdAndType(UUID postId, InteractionType type);

    void deleteByPostIdAndAccountIdAndType(UUID postId, UUID accountId, InteractionType type);

    void deleteByPostId(UUID postId);

    @Query("""
            select i.postId, count(i)
            from PostInteractionJpaEntity i
            where i.postId in :postIds and i.type = :type
            group by i.postId
            """)
    List<Object[]> countGroupedByPost(@Param("postIds") List<UUID> postIds, @Param("type") InteractionType type);

    @Query("""
            select i.postId
            from PostInteractionJpaEntity i
            where i.accountId = :accountId and i.type = :type and i.postId in :postIds
            """)
    List<UUID> findPostIdsByAccountAndType(
            @Param("postIds") List<UUID> postIds,
            @Param("accountId") UUID accountId,
            @Param("type") InteractionType type);
}
