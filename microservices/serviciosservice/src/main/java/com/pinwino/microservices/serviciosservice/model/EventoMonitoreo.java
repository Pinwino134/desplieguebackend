package com.pinwino.microservices.serviciosservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Evento capturado por el sistema de monitoreo (Prometheus, Gateway,
 * Microservicios, Base de Datos o detección operativa) siguiendo ITIL 4.
 */
@Entity
@Table(
    name = "evento_monitoreo",
    indexes = {
        @Index(name = "idx_evento_tipo", columnList = "tipo"),
        @Index(name = "idx_evento_estado", columnList = "estado"),
        @Index(name = "idx_evento_servicio", columnList = "servicio_origen"),
        @Index(name = "idx_evento_fecha", columnList = "fecha_deteccion")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoMonitoreo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @NotNull(message = "La clasificación del evento es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoEvento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private EstadoEvento estado;

    @NotBlank(message = "El origen o herramienta de monitoreo es obligatorio")
    @Column(name = "fuente_monitoreo", nullable = false, length = 50)
    private String fuenteMonitoreo;

    @NotBlank(message = "El servicio origen es obligatorio")
    @Column(name = "servicio_origen", nullable = false, length = 60)
    private String servicioOrigen;

    @NotBlank(message = "El título del evento es obligatorio")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String titulo;

    @Size(max = 1500)
    @Column(length = 1500)
    private String descripcion;

    @Column(length = 80)
    private String metrica;

    @Column(name = "valor_detectado", length = 60)
    private String valorDetectado;

    @Column(length = 60)
    private String umbral;

    @Enumerated(EnumType.STRING)
    @Column(name = "impacto_estimado", length = 10)
    private NivelImpacto impactoEstimado;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgencia_estimada", length = 10)
    private NivelUrgencia urgenciaEstimada;

    @Column(name = "incidente_generado_id")
    private Long incidenteGeneradoId;

    @Column(name = "incidente_generado_codigo", length = 30)
    private String incidenteGeneradoCodigo;

    @Column(name = "accion_respuesta", length = 1000)
    private String accionRespuesta;

    @CreationTimestamp
    @Column(name = "fecha_deteccion", nullable = false, updatable = false)
    private LocalDateTime fechaDeteccion;

    @Column(name = "fecha_atencion")
    private LocalDateTime fechaAtencion;
}
