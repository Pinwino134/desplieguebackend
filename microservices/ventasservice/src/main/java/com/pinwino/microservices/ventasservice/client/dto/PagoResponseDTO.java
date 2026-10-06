package com.pinwino.microservices.ventasservice.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PagoResponseDTO {
    private Long id;
    private Long pedidoId;
    private String estado;
}
