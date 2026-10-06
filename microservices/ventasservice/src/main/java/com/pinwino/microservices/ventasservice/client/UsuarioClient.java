package com.pinwino.microservices.ventasservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "authservice")
public interface UsuarioClient {

    @GetMapping("/api/v1/usuarios/{id}")
    UsuarioDTO buscarPorId(@PathVariable Long id);

    // Solo los campos que ventasservice necesita; Feign/Jackson ignora
    // el resto de la respuesta de authservice (incluido el password).
    record UsuarioDTO(Long id, String email, String nombres, String apellidos) {}
}
