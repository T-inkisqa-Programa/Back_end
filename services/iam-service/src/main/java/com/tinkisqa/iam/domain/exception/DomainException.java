package com.tinkisqa.iam.domain.exception;

/**
 * Excepción de negocio del dominio, independiente del framework web.
 *
 * <p>La capa de presentación traduce cada {@link Code} al código HTTP
 * correspondiente, manteniendo el dominio libre de dependencias HTTP.</p>
 */
public class DomainException extends RuntimeException {

    public enum Code {
        NOT_FOUND,
        CONFLICT,
        UNAUTHORIZED,
        FORBIDDEN,
        VALIDATION,
        BUSINESS_RULE,
        TOO_MANY_REQUESTS
    }

    private final Code code;

    public DomainException(Code code, String message) {
        super(message);
        this.code = code;
    }

    public Code code() {
        return code;
    }

    public static DomainException notFound(String message) {
        return new DomainException(Code.NOT_FOUND, message);
    }

    public static DomainException conflict(String message) {
        return new DomainException(Code.CONFLICT, message);
    }

    public static DomainException unauthorized(String message) {
        return new DomainException(Code.UNAUTHORIZED, message);
    }

    public static DomainException forbidden(String message) {
        return new DomainException(Code.FORBIDDEN, message);
    }

    public static DomainException validation(String message) {
        return new DomainException(Code.VALIDATION, message);
    }

    public static DomainException businessRule(String message) {
        return new DomainException(Code.BUSINESS_RULE, message);
    }

    public static DomainException tooManyRequests(String message) {
        return new DomainException(Code.TOO_MANY_REQUESTS, message);
    }
}
