package com.pinwino.microservices.ventasservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoRequestDTO {
    private Long pedidoId;
    private Long clienteId;
    private Long metodoPagoId;
    private BigDecimal monto;
}
