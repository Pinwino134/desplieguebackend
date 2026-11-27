package com.pinwino.microservice.gerenciaservice.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}
