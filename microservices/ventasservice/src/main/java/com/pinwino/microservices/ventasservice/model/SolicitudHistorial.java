package com.pinwino.microservices.ventasservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Bitácora de una solicitud: creación, comentarios, cambios de estado y asignaciones. */
@Entity
@Table(name = "solicitud_historial")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "solicitud_id", nullable = false)
    private Long solicitudId;

    @Column(name = "autor_id", nullable = false)
    private Long autorId;

    @Column(name = "autor_tipo", nullable = false, length = 20)
    private String autorTipo;

    // CREADA, COMENTARIO, CAMBIO_ESTADO, ASIGNADA, CAMBIO_PRIORIDAD
    @Column(nullable = false, length = 30)
    private String evento;

    @Column(length = 2000)
    private String detalle;

    @Column(nullable = false)
    private LocalDateTime fecha;
}
