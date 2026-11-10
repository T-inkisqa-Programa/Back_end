package com.tinkisqa.iam.infrastructure.event;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.tinkisqa.iam.domain.port.DomainEventPublisher;

/**
 * Publicador de eventos de dominio basado en log.
 *
 * <p>Punto de extensión para un bus de mensajería (RabbitMQ/Kafka) sin
 * modificaciones en el dominio ni en la capa de aplicación.</p>
 */
@Component
public class LoggingDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingDomainEventPublisher.class);

    @Override
    public void publish(String eventName, java.util.Map<String, Object> payload) {
        log.info("[EVENTO-DOMINIO] {} payload={}", eventName, payload);
    }
}
