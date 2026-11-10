package com.tinkisqa.social.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityMemberJpaRepository extends JpaRepository<CommunityMemberJpaEntity, UUID> {

    boolean existsByCommunityIdAndAccountId(UUID communityId, UUID accountId);

    long countByCommunityId(UUID communityId);

    List<CommunityMemberJpaEntity> findByAccountId(UUID accountId);

    void deleteByCommunityIdAndAccountId(UUID communityId, UUID accountId);

    /** Integrantes por comunidad (una sola consulta para todo el listado). */
    @Query("""
            select m.communityId, count(m)
            from CommunityMemberJpaEntity m
            where m.communityId in :communityIds
            group by m.communityId
            """)
    List<Object[]> countGroupedByCommunity(@Param("communityIds") List<UUID> communityIds);
}
