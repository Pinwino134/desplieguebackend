package com.pinwino.microservice.gerenciaservice.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ApiError {
    private int status;
    private String mensaje;
    private LocalDateTime timestamp;
}
