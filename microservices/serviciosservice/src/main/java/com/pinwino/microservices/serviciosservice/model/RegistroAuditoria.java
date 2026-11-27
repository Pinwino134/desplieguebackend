package com.pinwino.microservices.serviciosservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Bitácora inmutable de auditoría de accesos, aprobaciones y cambios críticos.
 */
@Entity
@Table(
    name = "registro_auditoria",
    indexes = {
        @Index(name = "idx_auditoria_fecha", columnList = "fecha_evento"),
        @Index(name = "idx_auditoria_usuario", columnList = "usuario_id"),
        @Index(name = "idx_auditoria_modulo", columnList = "modulo")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "usuario_email", length = 150)
    private String usuarioEmail;

    @Column(name = "usuario_rol", length = 30)
    private String usuarioRol;

    @NotBlank
    @Column(nullable = false, length = 60)
    private String modulo;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String accion;

    @Column(length = 150)
    private String recurso;

    @Column(length = 1000)
    private String detalle;

    @Column(nullable = false, length = 20)
    private String resultado;

    @Column(name = "ip_origen", length = 64)
    private String ipOrigen;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @CreationTimestamp
    @Column(name = "fecha_evento", nullable = false, updatable = false)
    private LocalDateTime fechaEvento;
}
