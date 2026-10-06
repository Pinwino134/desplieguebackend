package com.pinwino.microservices.ventasservice.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductoDTO {
    private Long id;
    private String nombre;
    private BigDecimal precioBase;
    private String estado;
    private Long artesanoId;
}
