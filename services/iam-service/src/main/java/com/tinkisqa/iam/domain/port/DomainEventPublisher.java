package com.tinkisqa.iam.domain.port;

/**
 * Puerto de salida: publicación de eventos de dominio.
 *
 * <p>En esta versión no hay broker: los eventos se registran en el log (y
 * quedan listos para publicarse a un bus de mensajería cuando se incorpore).
 * Esto permite que otros dominios (Social, Engagement) reaccionen a hechos como
 * {@code usuario.registrado} o {@code correo.verificado}.</p>
 */
public interface DomainEventPublisher {

    void publish(String eventName, java.util.Map<String, Object> payload);
}
