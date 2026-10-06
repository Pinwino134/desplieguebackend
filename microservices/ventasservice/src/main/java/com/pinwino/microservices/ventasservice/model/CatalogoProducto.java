package com.pinwino.microservices.ventasservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Replica de solo lectura del producto real (dueno: productos_db).
 * Se actualiza por eventos (ProductoCreado, ProductoActualizado, StockActualizado).
 * El id NO es autogenerado aqui: es el mismo id del producto en productos_db.
 */
@Entity
@Table(name = "catalogo_producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogoProducto {

    @Id
    private Long productoId;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;

    @Column(length = 80)
    private String categoria;

    @Column(name = "stock_disponible")
    private Integer stockDisponible;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}
