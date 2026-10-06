package com.pinwino.microservice.gerenciaservice.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PedidoDTO {
    private Long id;
    private Long clienteId;
    private String estado;
    private BigDecimal total;
}
