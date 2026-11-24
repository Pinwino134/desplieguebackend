package com.pinwino.microservices.serviciosservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "solicitud",
    indexes = {
        @Index(name = "idx_solicitud_usuario", columnList = "usuario_id"),
        @Index(name = "idx_solicitud_tipo", columnList = "tipo"),
        @Index(name = "idx_solicitud_estado", columnList = "estado"),
        @Index(name = "idx_solicitud_prioridad", columnList = "prioridad")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @NotNull(message = "El tipo de solicitud es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoSolicitud tipo;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private NivelImpacto impacto;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private NivelUrgencia urgencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private PrioridadSolicitud prioridad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_escalamiento", nullable = false, length = 25)
    private NivelEscalamiento nivelEscalamiento;

    @Column(name = "servicio_afectado", length = 60)
    private String servicioAfectado;

    @NotBlank(message = "El asunto es obligatorio")
    @Size(max = 150, message = "El asunto no puede superar los 150 caracteres")
    @Column(nullable = false, length = 150)
    private String asunto;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Column(length = 60)
    private String categoria;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Size(max = 120)
    @Column(name = "solicitante_nombre", length = 120)
    private String solicitanteNombre;

    @Email(message = "El correo del solicitante no es válido")
    @Size(max = 150)
    @Column(name = "solicitante_email", length = 150)
    private String solicitanteEmail;

    @Column(name = "solicitante_rol", length = 30)
    private String solicitanteRol;

    /** Aplica cuando tipo = ACCESO (ej. ARTESANO, ADMINISTRADOR, GERENCIA). */
    @Column(name = "rol_solicitado", length = 30)
    private String rolSolicitado;

    /** Referencia opcional cuando la solicitud/incidente está asociado a un pedido. */
    @Column(name = "pedido_id")
    private Long pedidoId;

    /** ID del evento de monitoreo que originó este incidente (si aplica). */
    @Column(name = "evento_origen_id")
    private Long eventoOrigenId;

    @Column(name = "asignado_a")
    private Long asignadoA;

    @Column(name = "asignado_nombre", length = 120)
    private String asignadoNombre;

    @Size(max = 1500)
    @Column(name = "solucion_temporal", length = 1500)
    private String solucionTemporal;

    @Size(max = 1500)
    @Column(name = "causa_raiz", length = 1500)
    private String causaRaiz;

    @Size(max = 2000)
    @Column(name = "respuesta_resolucion", length = 2000)
    private String respuestaResolucion;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_limite_respuesta_sla")
    private LocalDateTime fechaLimiteRespuestaSla;

    @Column(name = "fecha_primera_respuesta")
    private LocalDateTime fechaPrimeraRespuesta;

    @Column(name = "sla_respuesta_cumplido")
    private Boolean slaRespuestaCumplido;

    @Column(name = "fecha_limite_sla", nullable = false)
    private LocalDateTime fechaLimiteSla;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(name = "sla_cumplido")
    private Boolean slaCumplido;
}
