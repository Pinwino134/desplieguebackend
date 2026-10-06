package com.pinwino.microservices.productosservice.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Datos del producto que serán enviados a otros servicios.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {

    private Long id;
    private String nombre;
    private BigDecimal precioBase;
    private String estado;
    private Long artesanoId;
}
