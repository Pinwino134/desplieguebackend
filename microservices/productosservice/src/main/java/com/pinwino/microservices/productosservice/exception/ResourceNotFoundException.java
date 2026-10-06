package com.pinwino.microservices.productosservice.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String entidad, Long id) {
        super(entidad + " con id " + id + " no encontrado");
    }
}
