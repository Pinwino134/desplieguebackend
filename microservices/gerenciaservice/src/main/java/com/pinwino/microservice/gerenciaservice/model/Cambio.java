package com.pinwino.microservice.gerenciaservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Solicitud de cambio (RFC) sobre la plataforma. */
@Entity
@Table(name = "cambio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cambio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCambio tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ComponenteCambio componente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private NivelCambio impacto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private NivelCambio riesgo;

    // Por qué se necesita el cambio
    @Column(nullable = false, length = 1000)
    private String motivo;

    @Column(name = "plan_implementacion", nullable = false, length = 2000)
    private String planImplementacion;

    // Cómo volver atrás si sale mal
    @Column(name = "plan_reversa", nullable = false, length = 2000)
    private String planReversa;

    @Column(name = "plan_pruebas", length = 1000)
    private String planPruebas;

    @Column(name = "fecha_planificada")
    private LocalDateTime fechaPlanificada;

    @Column(name = "solicitante_id", nullable = false)
    private Long solicitanteId;

    @Column(name = "solicitante_tipo", nullable = false, length = 20)
    private String solicitanteTipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCambio estado;

    // Decisión del comité (gerencia). Nulos si fue preautorizado.
    @Column(name = "aprobador_id")
    private Long aprobadorId;

    @Column(name = "fecha_decision")
    private LocalDateTime fechaDecision;

    @Column(name = "motivo_decision", length = 1000)
    private String motivoDecision;

    // Implementación
    @Column(name = "implementador_id")
    private Long implementadorId;

    @Column(name = "fecha_implementacion")
    private LocalDateTime fechaImplementacion;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private ResultadoCambio resultado;

    @Column(name = "notas_implementacion", length = 1000)
    private String notasImplementacion;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @JsonProperty("codigo")
    public String getCodigo() {
        return id == null ? null : String.format("CAM-%04d", id);
    }
}
