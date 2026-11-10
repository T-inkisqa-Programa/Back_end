package com.tinkisqa.iam.infrastructure.persistence.jpa;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.tinkisqa.iam.domain.model.Account;
import com.tinkisqa.iam.domain.model.AccountStatus;
import com.tinkisqa.iam.domain.port.AccountRepository;

import jakarta.persistence.criteria.Predicate;

/** Adaptador que implementa {@link AccountRepository} sobre JPA. */
@Component
public class AccountRepositoryAdapter implements AccountRepository {

    private final AccountJpaRepository repository;

    public AccountRepositoryAdapter(AccountJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Account save(Account account) {
        AccountJpaEntity entity = toEntity(account);
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return repository.findById(id).map(AccountRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        return repository.findByEmailIgnoreCase(email).map(AccountRepositoryAdapter::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmailIgnoreCase(email);
    }

    private static AccountJpaEntity toEntity(Account account) {
        AccountJpaEntity entity = new AccountJpaEntity();
        entity.setId(account.id());
        entity.setEmail(account.email());
        entity.setPasswordHash(account.passwordHash());
        entity.setNombreCompleto(account.nombreCompleto());
        entity.setRole(account.role());
        entity.setAccountStatus(account.accountStatus());
        entity.setIdentityStatus(account.identityStatus());
        entity.setAccessStatus(account.accessStatus());
        entity.setEmailVerified(account.emailVerified());
        entity.setNormasAceptadas(account.normasAceptadas());
        entity.setBio(account.bio());
        entity.setGenero(account.genero());
        entity.setGeneroPersonalizado(account.generoPersonalizado());
        entity.setIntereses(new LinkedHashSet<>(account.intereses()));
        entity.setAvatarUri(account.avatarUri());
        entity.setCreatedAt(account.createdAt());
        entity.setUpdatedAt(account.updatedAt());
        return entity;
    }

    @Override
    public List<Account> search(String query, AccountStatus status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(Math.max(0, page), Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return repository.findAll(filter(query, status), pageRequest).getContent().stream()
                .map(AccountRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public long count(String query, AccountStatus status) {
        return repository.count(filter(query, status));
    }

    private static Specification<AccountJpaEntity> filter(String query, AccountStatus status) {
        return (root, criteriaQuery, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query != null && !query.isBlank()) {
                String like = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.<String>get("email")), like),
                        builder.like(builder.lower(root.<String>get("nombreCompleto")), like)));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("accountStatus"), status));
            }
            return predicates.isEmpty() ? builder.conjunction() : builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Account toDomain(AccountJpaEntity entity) {
        Set<String> intereses = new LinkedHashSet<>(entity.getIntereses());
        return new Account(
                entity.getId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getNombreCompleto(),
                entity.getRole(),
                entity.getAccountStatus(),
                entity.getIdentityStatus(),
                entity.getAccessStatus(),
                entity.isEmailVerified(),
                entity.isNormasAceptadas(),
                entity.getBio(),
                entity.getGenero(),
                entity.getGeneroPersonalizado(),
                intereses,
                entity.getAvatarUri(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
