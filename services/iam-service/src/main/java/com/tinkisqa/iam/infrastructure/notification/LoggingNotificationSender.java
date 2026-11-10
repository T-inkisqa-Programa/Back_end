package com.tinkisqa.iam.infrastructure.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.tinkisqa.iam.domain.port.NotificationSender;

/**
 * Implementación de desarrollo de {@link NotificationSender}.
 *
 * <p>No existe todavía una integración real de correo/SMS: el código se escribe
 * en el log para poder completar el flujo de verificación. Al conectar un
 * proveedor real (SMTP, Twilio, etc.) solo se reemplaza esta clase.</p>
 */
@Component
public class LoggingNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationSender.class);

    @Override
    public void sendEmailCode(String email, String code, String purpose) {
        log.info("[NOTIFICACION-DEV] Correo a '{}' ({}). Código de verificación: {}",
                email, purpose, code);
    }

    @Override
    public void sendSmsCode(String phone, String code, String purpose) {
        log.info("[NOTIFICACION-DEV] SMS a '{}' ({}). Código de verificación: {}",
                phone, purpose, code);
    }
}
