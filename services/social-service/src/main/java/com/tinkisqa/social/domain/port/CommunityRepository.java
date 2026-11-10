package com.tinkisqa.social.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.tinkisqa.social.domain.model.Community;

/** Puerto de salida: persistencia de comunidades. */
public interface CommunityRepository {

    Community save(Community community);

    Optional<Community> findById(UUID id);

    /** Lista/busca comunidades; {@code ownerId} filtra las creadas por una cuenta. */
    List<Community> search(String query, UUID ownerId, int page, int size);

    long count(String query, UUID ownerId);

    void deleteById(UUID id);
}
