package com.pinwino.microservices.productosservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Reconocimiento, premio o mención destacada registrada por un artesano.
 * Siempre nace en PENDIENTE_REVISION y solo se muestra al cliente cuando
 * un ADMINISTRADOR lo aprueba (estado = APROBADO).
 */
@Entity
@Table(
    name = "logro_artesano",
    indexes = {
        @Index(name = "idx_logro_artesano", columnList = "artesano_id"),
        @Index(name = "idx_logro_estado", columnList = "estado")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogroArtesano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "artesano_id", nullable = false)
    private Long artesanoId;

    @Size(max = 150)
    @Column(name = "artesano_nombre", length = 150)
    private String artesanoNombre;

    @NotBlank(message = "El título del reconocimiento es obligatorio")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String titulo;

    @Size(max = 150)
    @Column(length = 150)
    private String institucion;

    private Integer anio;

    @NotBlank(message = "Describe brevemente el reconocimiento o trayectoria")
    @Size(max = 1000)
    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Size(max = 500)
    @Column(name = "evidencia_url", length = 500)
    private String evidenciaUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private EstadoRevisionLogro estado;

    @Size(max = 500)
    @Column(name = "comentario_admin", length = 500)
    private String comentarioAdmin;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    @PrePersist
    protected void onCreate() {
        if (this.estado == null) {
            this.estado = EstadoRevisionLogro.PENDIENTE_REVISION;
        }
    }
}
