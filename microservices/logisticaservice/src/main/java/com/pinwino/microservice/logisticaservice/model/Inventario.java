package com.pinwino.microservice.logisticaservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "almacen_id", nullable = false)
    @JsonBackReference
    private Almacen almacen;

    @NotNull
    @Column(name = "producto_id", nullable = false)
    private Long productoId; // ref externa a productos_db

    @Builder.Default
    @Column(name = "cantidad_disponible", nullable = false)
    private Integer cantidadDisponible = 0;

    @Builder.Default
    @Column(name = "cantidad_reservada", nullable = false)
    private Integer cantidadReservada = 0;

    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PreUpdate
    @PrePersist
    protected void onChange() {
        this.actualizadoEn = LocalDateTime.now();
    }
}
