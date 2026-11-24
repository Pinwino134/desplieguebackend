package com.pinwino.microservices.serviciosservice;

import com.pinwino.microservices.serviciosservice.model.*;
import com.pinwino.microservices.serviciosservice.repository.EventoMonitoreoRepository;
import com.pinwino.microservices.serviciosservice.service.AuditoriaService;
import com.pinwino.microservices.serviciosservice.service.SolicitudService;
import com.pinwino.microservices.serviciosservice.service.impl.EventoMonitoreoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoMonitoreoServiceImplTest {

    @Mock private EventoMonitoreoRepository eventoRepository;
    @Mock private SolicitudService solicitudService;
    @Mock private AuditoriaService auditoriaService;

    private EventoMonitoreoServiceImpl eventoService;

    @BeforeEach
    void setUp() {
        eventoService = new EventoMonitoreoServiceImpl(eventoRepository, solicitudService, auditoriaService);
    }

    @Test
    void registrarEventoExcepcionAsignaCodigoEVXYSeveridadAlta() {
        EventoMonitoreo ev = EventoMonitoreo.builder()
                .tipo(TipoEvento.EXCEPCION)
                .fuenteMonitoreo("PROMETHEUS")
                .servicioOrigen("apigateway")
                .titulo("Tasa de errores 5xx > 5%")
                .metrica("http_server_requests_5xx")
                .valorDetectado("8.4%")
                .umbral("5%")
                .build();

        when(eventoRepository.save(any(EventoMonitoreo.class))).thenAnswer(inv -> inv.getArgument(0));

        EventoMonitoreo guardado = eventoService.registrar(ev, "corr-ev1");

        assertTrue(guardado.getCodigo().startsWith("EVX-"));
        assertEquals(EstadoEvento.DETECTADO, guardado.getEstado());
        assertEquals(NivelImpacto.ALTO, guardado.getImpactoEstimado());
        assertEquals(NivelUrgencia.ALTA, guardado.getUrgenciaEstimada());
        verify(auditoriaService, times(1)).registrar(any(RegistroAuditoria.class));
    }

    @Test
    void escalarEventoAIncidenteCreaIncidenteYActualizaEvento() {
        EventoMonitoreo ev = EventoMonitoreo.builder()
                .id(10L)
                .codigo("EVX-2026-55555")
                .tipo(TipoEvento.EXCEPCION)
                .estado(EstadoEvento.DETECTADO)
                .fuenteMonitoreo("PROMETHEUS")
                .servicioOrigen("postgres")
                .titulo("Conexiones HikariCP agotadas")
                .impactoEstimado(NivelImpacto.ALTO)
                .urgenciaEstimada(NivelUrgencia.ALTA)
                .build();

        Solicitud incCreado = Solicitud.builder()
                .id(99L)
                .codigo("INC-2026-99999")
                .tipo(TipoSolicitud.INCIDENTE)
                .prioridad(PrioridadSolicitud.CRITICA)
                .nivelEscalamiento(NivelEscalamiento.N2_ADMINISTRADOR)
                .build();

        when(eventoRepository.findById(10L)).thenReturn(Optional.of(ev));
        when(solicitudService.crear(any(Solicitud.class), any(), any())).thenReturn(incCreado);
        when(eventoRepository.save(any(EventoMonitoreo.class))).thenAnswer(inv -> inv.getArgument(0));

        Solicitud resultado = eventoService.escalarAIncidente(
                10L, 1L, "admin@arteandino.pe", "ADMINISTRADOR", "127.0.0.1", "corr-esc-ev");

        assertEquals("INC-2026-99999", resultado.getCodigo());
        assertEquals(EstadoEvento.ESCALADO_A_INCIDENTE, ev.getEstado());
        assertEquals(99L, ev.getIncidenteGeneradoId());
    }
}
