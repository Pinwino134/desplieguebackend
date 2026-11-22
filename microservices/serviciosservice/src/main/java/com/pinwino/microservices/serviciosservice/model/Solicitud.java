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
        @Index(name = "idx_solicitud_estado", columnList = "estado")
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
    @Column(nullable = false, length = 15)
    private PrioridadSolicitud prioridad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado;

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

    /** Referencia opcional cuando la solicitud está asociada a un pedido. */
    @Column(name = "pedido_id")
    private Long pedidoId;

    @Column(name = "asignado_a")
    private Long asignadoA;

    @Column(name = "asignado_nombre", length = 120)
    private String asignadoNombre;

    @Size(max = 2000)
    @Column(name = "respuesta_resolucion", length = 2000)
    private String respuestaResolucion;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_limite_sla", nullable = false)
    private LocalDateTime fechaLimiteSla;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(name = "sla_cumplido")
    private Boolean slaCumplido;
}
