package com.pinwino.microservice.gerenciaservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/** Se llena por evento/ETL desde logistica_db. */
@Entity
@Table(name = "reporte_inventario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 7)
    private String periodo;

    @Column(name = "almacen_id", nullable = false)
    private Long almacenId; // ref externa a logistica_db

    private BigDecimal rotacion;

    @Column(name = "productos_stock_critico")
    private Integer productosStockCritico;
}
