package com.tinkisqa.iam.domain.port;

/**
 * Puerto de salida: envío de códigos de verificación.
 *
 * <p>La implementación de desarrollo escribe el código en el log del servicio
 * (no existe integración real de correo/SMS todavía); el contrato permite
 * enchufar un proveedor real más adelante sin tocar el dominio.</p>
 */
public interface NotificationSender {

    void sendEmailCode(String email, String code, String purpose);

    void sendSmsCode(String phone, String code, String purpose);
}
