package com.pinwino.microservice.gerenciaservice.exception;

/** El usuario no tiene permiso para esta acción (responde HTTP 403). */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) { super(message); }
}
