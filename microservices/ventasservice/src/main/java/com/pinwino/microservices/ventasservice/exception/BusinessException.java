package com.pinwino.microservices.ventasservice.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}
