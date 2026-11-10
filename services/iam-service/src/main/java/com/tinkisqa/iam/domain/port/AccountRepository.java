package com.tinkisqa.iam.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.tinkisqa.iam.domain.model.Account;
import com.tinkisqa.iam.domain.model.AccountStatus;

/** Puerto de salida: persistencia de cuentas. */
public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findById(UUID id);

    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Búsqueda paginada para el panel de administración.
     *
     * @param query  texto libre que se compara con el correo y el nombre (puede ser nulo)
     * @param status filtra por estado de cuenta (puede ser nulo)
     */
    List<Account> search(String query, AccountStatus status, int page, int size);

    /** Total de cuentas que cumplen el mismo filtro que {@link #search}. */
    long count(String query, AccountStatus status);
}
