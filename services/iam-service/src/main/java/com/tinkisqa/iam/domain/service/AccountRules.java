package com.tinkisqa.iam.domain.service;

import java.util.regex.Pattern;

/**
 * Reglas puras de validación de datos de registro y perfil.
 *
 * <p>Replican las validaciones del frontend para que el backend sea la fuente
 * de verdad: contraseña de al menos 10 caracteres con 4 comprobaciones,
 * nombres de al menos 3 caracteres con nombre y apellido, DNI de 8 dígitos y
 * teléfono de 9 dígitos.</p>
 */
public final class AccountRules {

    public static final int PASSWORD_MIN_LENGTH = 10;
    public static final int NAME_MIN_LENGTH = 3;
    public static final int BIO_MAX_LENGTH = 280;

    private static final java.util.regex.Pattern EMAIL =
            java.util.regex.Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.-]{2,}$");
    private static final java.util.regex.Pattern DNI =
            java.util.regex.Pattern.compile("^\\d{8}$");
    private static final java.util.regex.Pattern PHONE =
            java.util.regex.Pattern.compile("^\\d{9}$");

    private AccountRules() {
    }

    public static boolean isEmailValid(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    public static boolean isDniValid(String dni) {
        return dni != null && DNI.matcher(dni.trim()).matches();
    }

    public static boolean isPhoneValid(String phone) {
        return phone != null && PHONE.matcher(phone.trim()).matches();
    }

    /** Nombres y apellidos: al menos dos palabras de 3 o más caracteres. */
    public static boolean isNombreCompletoValid(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return false;
        }
        String[] parts = nombre.trim().split("\\s+");
        if (parts.length < 2) {
            return false;
        }
        return java.util.Arrays.stream(parts).allMatch(p -> p.length() >= NAME_MIN_LENGTH);
    }

    /**
     * Fuerza de la contraseña: longitud mínima y al menos 3 de estas 4
     * comprobaciones (mayúscula, minúscula, dígito y símbolo).
     */
    public static PasswordStrength passwordStrength(String password) {
        String value = password == null ? "" : password;
        boolean length = value.length() >= PASSWORD_MIN_LENGTH;
        boolean upper = value.chars().anyMatch(Character::isUpperCase);
        boolean lower = value.chars().anyMatch(Character::isLowerCase);
        boolean digit = value.chars().anyMatch(Character::isDigit);
        boolean symbol = value.chars().anyMatch(c -> !Character.isLetterOrDigit(c));

        int checks = (upper ? 1 : 0) + (lower ? 1 : 0) + (digit ? 1 : 0) + (symbol ? 1 : 0);
        return new PasswordStrength(length, upper, lower, digit, symbol, length && checks >= 3);
    }

    public record PasswordStrength(
            boolean length,
            boolean upper,
            boolean lower,
            boolean digit,
            boolean symbol,
            boolean acceptable) {
    }
}
