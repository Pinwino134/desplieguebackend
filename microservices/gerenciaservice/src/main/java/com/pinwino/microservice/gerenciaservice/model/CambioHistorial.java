package com.pinwino.microservice.gerenciaservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Bitácora de un cambio: creación, comentarios, decisiones e implementación. */
@Entity
@Table(name = "cambio_historial")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambioHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cambio_id", nullable = false)
    private Long cambioId;

    @Column(name = "autor_id", nullable = false)
    private Long autorId;

    @Column(name = "autor_tipo", nullable = false, length = 20)
    private String autorTipo;

    // CREADO, COMENTARIO, CAMBIO_ESTADO, DECISION, IMPLEMENTACION
    @Column(nullable = false, length = 30)
    private String evento;

    @Column(length = 2000)
    private String detalle;

    @Column(nullable = false)
    private LocalDateTime fecha;
}
