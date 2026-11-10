package com.tinkisqa.iam.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.tinkisqa.iam.domain.model.Verification;
import com.tinkisqa.iam.domain.model.VerificationState;

/** Puerto de salida: persistencia de verificaciones de identidad. */
public interface VerificationRepository {

    Verification save(Verification verification);

    Optional<Verification> findById(UUID id);

    Optional<Verification> findLatestByAccountId(UUID accountId);

    boolean existsByDniFingerprint(String dniFingerprint);

    /** Verificaciones en un estado concreto, de la más antigua a la más reciente. */
    List<Verification> findByState(VerificationState state, int limit);

    /** Cola de moderación: verificaciones aún abiertas (pendientes o en revisión humana). */
    List<Verification> findOpen(int limit);
}
