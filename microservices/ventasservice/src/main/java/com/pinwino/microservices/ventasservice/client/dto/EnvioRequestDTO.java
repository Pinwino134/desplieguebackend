package com.pinwino.microservices.ventasservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnvioRequestDTO {
    private Long pedidoId;
    private String direccionDestino;
}
