package com.pinwino.microservice.logisticaservice.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String entidad, Long id) {
        super(entidad + " con id " + id + " no encontrado");
    }
}
