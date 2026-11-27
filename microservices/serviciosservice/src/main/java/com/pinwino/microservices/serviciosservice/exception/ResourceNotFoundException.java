package com.pinwino.microservices.serviciosservice.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String recurso, Long id) {
        super(recurso + " con ID " + id + " no encontrado");
    }
}
