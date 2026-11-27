package com.pinwino.microservices.ventasservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Solicitud de servicio (soporte, consulta, reclamo, etc.) hecha por un usuario.
 * El solicitante es el id del usuario en authservice (viene del token, no del cliente).
 */
@Entity
@Table(name = "solicitud")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "solicitante_id", nullable = false)
    private Long solicitanteId;

    @Column(name = "solicitante_tipo", nullable = false, length = 20)
    private String solicitanteTipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoSolicitud tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrioridadSolicitud prioridad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado;

    @Column(nullable = false, length = 150)
    private String asunto;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    // Pedido relacionado (opcional)
    @Column(name = "pedido_id")
    private Long pedidoId;

    // Administrador responsable (opcional hasta que alguien la toma)
    @Column(name = "asignado_a")
    private Long asignadoA;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Fecha máxima para resolver según el SLA de la prioridad
    @Column(name = "fecha_limite", nullable = false)
    private LocalDateTime fechaLimite;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    // ---- Campos calculados que se envían al frontend ----

    @JsonProperty("codigo")
    public String getCodigo() {
        return id == null ? null : String.format("SOL-%04d", id);
    }

    /**
     * Vencida = sigue sin resolverse y ya pasó su fecha límite.
     * En EN_ESPERA el reloj se considera pausado (se espera al usuario).
     */
    @JsonProperty("vencida")
    public boolean isVencida() {
        return (estado == EstadoSolicitud.REGISTRADA || estado == EstadoSolicitud.EN_ATENCION)
                && fechaLimite != null
                && LocalDateTime.now().isAfter(fechaLimite);
    }
}
