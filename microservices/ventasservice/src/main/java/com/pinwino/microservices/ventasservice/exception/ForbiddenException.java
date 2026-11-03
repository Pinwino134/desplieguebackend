package com.pinwino.microservices.ventasservice.exception;

/** El usuario no tiene permiso sobre el recurso (responde HTTP 403). */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
