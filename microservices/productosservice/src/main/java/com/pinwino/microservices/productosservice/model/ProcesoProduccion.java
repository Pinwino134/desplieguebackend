package com.pinwino.microservices.productosservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "proceso_produccion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcesoProduccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Producto al que pertenece este proceso de fabricación.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonBackReference
    private Producto producto;

    // Nombre de la etapa.
    @NotBlank
    @Column(nullable = false, length = 100)
    private String etapa;

    // Descripción de lo que se realiza en esta etapa.
    @Column(length = 255)
    private String descripcion;

    // Tiempo estimado para completar la etapa.
    @Min(0)
    @Column(name = "duracion_estimada_horas")
    private Integer duracionEstimadaHoras;

    // Orden en el que debe ejecutarse la etapa.
    @Min(1)
    @Column(name = "orden", nullable = false)
    private Integer ordenEtapa;
}