package com.pinwino.microservices.ventasservice.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventarioDTO {
    private Long id;
    private Long productoId;
    private Integer cantidadDisponible;
    private Integer cantidadReservada;
}
