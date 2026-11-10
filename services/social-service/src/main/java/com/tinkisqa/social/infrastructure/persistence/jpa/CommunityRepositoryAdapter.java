package com.tinkisqa.social.infrastructure.persistence.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.tinkisqa.social.domain.model.Community;
import com.tinkisqa.social.domain.port.CommunityRepository;

import jakarta.persistence.criteria.Predicate;

/** Adaptador JPA del puerto {@link CommunityRepository}. */
@Component
public class CommunityRepositoryAdapter implements CommunityRepository {

    private final CommunityJpaRepository repository;

    public CommunityRepositoryAdapter(CommunityJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Community save(Community community) {
        return toDomain(repository.save(toEntity(community)));
    }

    @Override
    public Optional<Community> findById(UUID id) {
        return repository.findById(id).map(CommunityRepositoryAdapter::toDomain);
    }

    @Override
    public List<Community> search(String query, UUID ownerId, int page, int size) {
        return repository
                .findAll(filter(query, ownerId),
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(CommunityRepositoryAdapter::toDomain)
                .getContent();
    }

    @Override
    public long count(String query, UUID ownerId) {
        return repository.count(filter(query, ownerId));
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private static Specification<CommunityJpaEntity> filter(String query, UUID ownerId) {
        return (root, criteriaQuery, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (ownerId != null) {
                predicates.add(builder.equal(root.get("ownerId"), ownerId));
            }
            if (query != null && !query.isBlank()) {
                String like = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.<String>get("name")), like),
                        builder.like(builder.lower(root.<String>get("description")), like)));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static CommunityJpaEntity toEntity(Community community) {
        CommunityJpaEntity entity = new CommunityJpaEntity();
        entity.setId(community.id());
        entity.setName(community.name());
        entity.setDescription(community.description());
        entity.setImageUrl(community.imageUrl());
        entity.setCategory(community.category());
        entity.setPrivacy(community.privacy());
        entity.setOwnerId(community.ownerId());
        entity.setCreatedAt(community.createdAt());
        entity.setUpdatedAt(community.updatedAt());
        return entity;
    }

    private static Community toDomain(CommunityJpaEntity entity) {
        return new Community(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getImageUrl(),
                entity.getCategory(),
                entity.getPrivacy(),
                entity.getOwnerId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
