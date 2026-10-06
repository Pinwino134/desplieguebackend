package com.pinwino.microservice.gerenciaservice.service;

import com.pinwino.microservice.gerenciaservice.model.*;
import java.util.List;

/**
 * Los metodos "upsert*" NO estan pensados para ser llamados por un usuario final:
 * los invoca el listener de eventos (o el job de ETL) que sincroniza datos desde
 * ventas_db, logistica_db, productos_db y pagos_db. El resto del sistema (y el
 * Gateway) debe exponer esta capa como solo lectura.
 */
public interface ReporteService {
    List<ReporteVentas> listarVentas();
    ReporteVentas upsertVentas(ReporteVentas reporte);

    List<ReporteInventario> listarInventarioPorPeriodo(String periodo);
    ReporteInventario upsertInventario(ReporteInventario reporte);

    List<ReporteArtesanos> listarArtesanosPorPeriodo(String periodo);
    ReporteArtesanos upsertArtesanos(ReporteArtesanos reporte);

    ReportePagos buscarPagosPorPeriodo(String periodo);
    ReportePagos upsertPagos(ReportePagos reporte);

    // Alternativa a los eventos: arma el reporte del periodo actual jalando en vivo
    // desde ventas-service via Feign, en vez de esperar a que alguien haga upsertVentas().
    ReporteVentas generarReporteVentasEnVivo(String periodo);
}
