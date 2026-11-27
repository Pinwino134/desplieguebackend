package com.pinwino.microservice.gerenciaservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Datos para proponer un cambio (los enums llegan como texto y se validan en el servicio). */
@Getter
@Setter
public class CambioRequest {
    private String titulo;
    private String descripcion;
    private String tipo;
    private String componente;
    private String impacto;
    private String riesgo;
    private String motivo;
    private String planImplementacion;
    private String planReversa;
    private String planPruebas;
    private LocalDateTime fechaPlanificada;
}
