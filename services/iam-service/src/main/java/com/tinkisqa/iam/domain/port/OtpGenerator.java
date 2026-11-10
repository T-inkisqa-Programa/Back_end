package com.tinkisqa.iam.domain.port;

/**
 * Puerto de salida: generación de códigos OTP y del hash de verificación.
 * Se abstrae para poder sustituir la implementación (segura o de pruebas).
 */
public interface OtpGenerator {

    /** Código numérico de 6 dígitos. */
    String generateCode();

    /** Hash determinista del código (HMAC) para verificar sin guardarlo en claro. */
    String hash(String code);

    boolean matches(String code, String codeHash);

    /**
     * Huella no reversible del DNI (HMAC con sal), usada para detectar el mismo
     * documento en dos cuentas sin almacenar el número.
     */
    String fingerprintDocument(String dni);
}
