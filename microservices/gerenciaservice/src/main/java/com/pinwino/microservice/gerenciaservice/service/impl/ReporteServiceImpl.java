package com.pinwino.microservice.gerenciaservice.service.impl;

import com.pinwino.microservice.gerenciaservice.client.VentasClient;
import com.pinwino.microservice.gerenciaservice.client.dto.PedidoDTO;
import com.pinwino.microservice.gerenciaservice.exception.ResourceNotFoundException;
import com.pinwino.microservice.gerenciaservice.model.*;
import com.pinwino.microservice.gerenciaservice.repository.*;
import com.pinwino.microservice.gerenciaservice.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private final ReporteVentasRepository reporteVentasRepository;
    private final ReporteInventarioRepository reporteInventarioRepository;
    private final ReporteArtesanosRepository reporteArtesanosRepository;
    private final ReportePagosRepository reportePagosRepository;
    private final VentasClient ventasClient;

    @Override @Transactional(readOnly = true)
    public List<ReporteVentas> listarVentas() { return reporteVentasRepository.findAllByOrderByPeriodoDesc(); }

    @Override @Transactional
    public ReporteVentas upsertVentas(ReporteVentas reporte) {
        return reporteVentasRepository.findByPeriodo(reporte.getPeriodo())
                .map(existente -> {
                    existente.setTotalVentas(reporte.getTotalVentas());
                    existente.setTotalPedidos(reporte.getTotalPedidos());
                    existente.setTicketPromedio(reporte.getTicketPromedio());
                    return reporteVentasRepository.save(existente);
                })
                .orElseGet(() -> reporteVentasRepository.save(reporte));
    }

    @Override @Transactional(readOnly = true)
    public List<ReporteInventario> listarInventarioPorPeriodo(String periodo) {
        return reporteInventarioRepository.findByPeriodo(periodo);
    }

    @Override @Transactional
    public ReporteInventario upsertInventario(ReporteInventario reporte) {
        return reporteInventarioRepository.save(reporte);
    }

    @Override @Transactional(readOnly = true)
    public List<ReporteArtesanos> listarArtesanosPorPeriodo(String periodo) {
        return reporteArtesanosRepository.findByPeriodo(periodo);
    }

    @Override @Transactional
    public ReporteArtesanos upsertArtesanos(ReporteArtesanos reporte) {
        return reporteArtesanosRepository.save(reporte);
    }

    @Override @Transactional(readOnly = true)
    public ReportePagos buscarPagosPorPeriodo(String periodo) {
        return reportePagosRepository.findByPeriodo(periodo)
                .orElseThrow(() -> new ResourceNotFoundException("ReportePagos para periodo " + periodo, 0L));
    }

    @Override @Transactional
    public ReportePagos upsertPagos(ReportePagos reporte) {
        return reportePagosRepository.findByPeriodo(reporte.getPeriodo())
                .map(existente -> {
                    existente.setMontoTotalAprobado(reporte.getMontoTotalAprobado());
                    existente.setMontoTotalRechazado(reporte.getMontoTotalRechazado());
                    existente.setTotalReembolsos(reporte.getTotalReembolsos());
                    return reportePagosRepository.save(existente);
                })
                .orElseGet(() -> reportePagosRepository.save(reporte));
    }

    @Override
    @Transactional
    public ReporteVentas generarReporteVentasEnVivo(String periodo) {
        // Llamada sincrona a ventas-service: trae los pedidos entregados y agrega en vivo.
        List<PedidoDTO> entregados = ventasClient.listarPorEstado("ENTREGADO");

        BigDecimal total = entregados.stream()
                .map(PedidoDTO::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int cantidad = entregados.size();
        BigDecimal ticketPromedio = cantidad == 0
                ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(cantidad), 2, java.math.RoundingMode.HALF_UP);

        ReporteVentas reporte = ReporteVentas.builder()
                .periodo(periodo)
                .totalVentas(total)
                .totalPedidos(cantidad)
                .ticketPromedio(ticketPromedio)
                .build();

        return upsertVentas(reporte);
    }
}
