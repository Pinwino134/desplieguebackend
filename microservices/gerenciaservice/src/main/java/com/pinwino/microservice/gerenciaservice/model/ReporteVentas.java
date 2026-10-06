package com.pinwino.microservice.gerenciaservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/** Se llena por evento/ETL desde ventas_db. No expone escritura publica. */
@Entity
@Table(name = "reporte_ventas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteVentas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 7) // "YYYY-MM"
    private String periodo;

    @Column(name = "total_ventas", precision = 12, scale = 2)
    private BigDecimal totalVentas;

    @Column(name = "total_pedidos")
    private Integer totalPedidos;

    @Column(name = "ticket_promedio", precision = 10, scale = 2)
    private BigDecimal ticketPromedio;
}
