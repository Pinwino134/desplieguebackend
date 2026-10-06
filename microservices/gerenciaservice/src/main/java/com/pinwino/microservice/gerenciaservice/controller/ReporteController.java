package com.pinwino.microservice.gerenciaservice.controller;

import com.pinwino.microservice.gerenciaservice.model.*;
import com.pinwino.microservice.gerenciaservice.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Endpoints de solo lectura para el dashboard de Gerencia.
 * Los PUT "/sync/..." son de uso INTERNO: los llama el listener de eventos
 * (Kafka/RabbitMQ) o el job de ETL que sincroniza desde ventas_db, logistica_db,
 * productos_db y pagos_db. No deben exponerse a traves del Gateway a usuarios finales.
 */
@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
public class ReporteController {
    private final ReporteService reporteService;

    @GetMapping("/ventas")
    public ResponseEntity<List<ReporteVentas>> listarVentas() { return ResponseEntity.ok(reporteService.listarVentas()); }

    @PutMapping("/sync/ventas")
    public ResponseEntity<ReporteVentas> sincronizarVentas(@RequestBody ReporteVentas reporte) {
        return ResponseEntity.ok(reporteService.upsertVentas(reporte));
    }

    // Alternativa al PUT de arriba: en vez de que alguien empuje el reporte ya calculado,
    // gerencia-service lo arma en vivo llamando a ventas-service por Feign.
    @PostMapping("/ventas/generar")
    public ResponseEntity<ReporteVentas> generarReporteVentas(@RequestParam String periodo) {
        return ResponseEntity.ok(reporteService.generarReporteVentasEnVivo(periodo));
    }

    @GetMapping("/inventario")
    public ResponseEntity<List<ReporteInventario>> listarInventario(@RequestParam String periodo) {
        return ResponseEntity.ok(reporteService.listarInventarioPorPeriodo(periodo));
    }

    @PutMapping("/sync/inventario")
    public ResponseEntity<ReporteInventario> sincronizarInventario(@RequestBody ReporteInventario reporte) {
        return ResponseEntity.ok(reporteService.upsertInventario(reporte));
    }

    @GetMapping("/artesanos")
    public ResponseEntity<List<ReporteArtesanos>> listarArtesanos(@RequestParam String periodo) {
        return ResponseEntity.ok(reporteService.listarArtesanosPorPeriodo(periodo));
    }

    @PutMapping("/sync/artesanos")
    public ResponseEntity<ReporteArtesanos> sincronizarArtesanos(@RequestBody ReporteArtesanos reporte) {
        return ResponseEntity.ok(reporteService.upsertArtesanos(reporte));
    }

    @GetMapping("/pagos")
    public ResponseEntity<ReportePagos> buscarPagos(@RequestParam String periodo) {
        return ResponseEntity.ok(reporteService.buscarPagosPorPeriodo(periodo));
    }

    @PutMapping("/sync/pagos")
    public ResponseEntity<ReportePagos> sincronizarPagos(@RequestBody ReportePagos reporte) {
        return ResponseEntity.ok(reporteService.upsertPagos(reporte));
    }
}
