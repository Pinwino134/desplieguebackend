package com.pinwino.microservices.productosservice.dto;

import com.pinwino.microservices.productosservice.model.EstadoProduccion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class OrdenProduccionDTO {

    private Long id;

    private Long pedidoId;

    private Long productoId;

    private String nombreProducto;

    private Integer cantidad;

    private LocalDate fechaInicio;

    private Integer avance;

    private EstadoProduccion estado;
}
