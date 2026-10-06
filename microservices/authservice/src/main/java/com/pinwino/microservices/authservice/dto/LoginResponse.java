package com.pinwino.microservices.authservice.dto;

import com.pinwino.microservices.authservice.model.TipoActor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tipo; // "Bearer"
    private Long usuarioId;
    private String email;
    private String nombres;
    private String apellidos;
    private TipoActor tipoActor;
}
