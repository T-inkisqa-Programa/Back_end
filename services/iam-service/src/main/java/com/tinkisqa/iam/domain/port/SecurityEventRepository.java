package com.tinkisqa.iam.domain.port;

import java.util.List;
import java.util.UUID;

import com.tinkisqa.iam.domain.model.SecurityEvent;

/** Puerto de salida: bitácora de eventos de seguridad. */
public interface SecurityEventRepository {

    SecurityEvent save(SecurityEvent event);

    List<SecurityEvent> findRecentByAccount(UUID accountId, int limit);
}
