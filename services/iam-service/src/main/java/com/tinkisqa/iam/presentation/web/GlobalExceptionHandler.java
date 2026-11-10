package com.tinkisqa.iam.presentation.web;

import java.net.URI;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.tinkisqa.iam.domain.exception.DomainException;

/**
 * Manejo centralizado de errores conforme a RFC 7807 ({@link ProblemDetail}).
 *
 * <p>Traduce cada código de {@link DomainException} a un estado HTTP y evita
 * filtrar detalles internos al cliente.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final Map<DomainException.Code, HttpStatus> STATUS = new EnumMap<>(DomainException.Code.class);

    static {
        STATUS.put(DomainException.Code.NOT_FOUND, HttpStatus.NOT_FOUND);
        STATUS.put(DomainException.Code.CONFLICT, HttpStatus.CONFLICT);
        STATUS.put(DomainException.Code.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
        STATUS.put(DomainException.Code.FORBIDDEN, HttpStatus.FORBIDDEN);
        STATUS.put(DomainException.Code.VALIDATION, HttpStatus.BAD_REQUEST);
        STATUS.put(DomainException.Code.BUSINESS_RULE, HttpStatus.UNPROCESSABLE_ENTITY);
        STATUS.put(DomainException.Code.TOO_MANY_REQUESTS, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomain(DomainException ex) {
        HttpStatus status = STATUS.getOrDefault(ex.code(), HttpStatus.BAD_REQUEST);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problem.setTitle("Regla de negocio");
        problem.setType(URI.create("https://tinkisqa.dev/errors/" + ex.code().name().toLowerCase(Locale.ROOT)));
        problem.setProperty("code", ex.code().name());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Revisa los datos enviados.");
        problem.setTitle("Validación fallida");
        problem.setType(URI.create("https://tinkisqa.dev/errors/validation"));
        ex.getBindingResult().getFieldErrors().forEach(error ->
                problem.setProperty(error.getField(), error.getDefaultMessage()));
        ex.getBindingResult().getGlobalErrors().forEach(error ->
                problem.setProperty(error.getObjectName(), error.getDefaultMessage()));
        return problem;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, "No tienes permisos para realizar esta acción.");
        problem.setTitle("Acceso denegado");
        return problem;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, "Debes iniciar sesión.");
        problem.setTitle("No autenticado");
        return problem;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParameter(MissingServletRequestParameterException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Falta el parámetro obligatorio: " + ex.getParameterName());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        log.error("Error no controlado", ex);
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado.");
    }
}
