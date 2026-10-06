package com.pinwino.microservices.productosservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "orden_produccion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenProduccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Pedido que originó la producción.
    @NotNull
    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    // Artesano responsable de la producción.
    // El artesano pertenece al authservice.
    @NotNull
    @Column(name = "artesano_id", nullable = false)
    private Long artesanoId;

    // Producto que se va a fabricar.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonBackReference
    private Producto producto;

    // Cantidad de productos que se deben fabricar.
    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer cantidad;

    // Fecha en la que comienza la producción.
    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    // Porcentaje de avance: 0 a 100.
    @Builder.Default
    @Min(0)
    @Max(100)
    @Column(nullable = false)
    private Integer avance = 0;

    // Estado actual de la producción.
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoProduccion estado = EstadoProduccion.PENDIENTE;
}