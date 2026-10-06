package com.pinwino.microservice.gerenciaservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/** Se llena por evento/ETL desde productos_db + ventas_db. */
@Entity
@Table(name = "reporte_artesanos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteArtesanos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 7)
    private String periodo;

    @Column(name = "artesano_id", nullable = false)
    private Long artesanoId; // ref externa a productos_db

    @Column(name = "productos_vendidos")
    private Integer productosVendidos;

    @Column(name = "ingresos_generados", precision = 12, scale = 2)
    private BigDecimal ingresosGenerados;
}
