package com.pinwino.microservices.serviciosservice;

import com.pinwino.microservices.serviciosservice.exception.BusinessException;
import com.pinwino.microservices.serviciosservice.model.*;
import com.pinwino.microservices.serviciosservice.repository.SolicitudRepository;
import com.pinwino.microservices.serviciosservice.service.AuditoriaService;
import com.pinwino.microservices.serviciosservice.service.impl.SolicitudServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudServiceImplTest {

    @Mock
    private SolicitudRepository solicitudRepository;
    @Mock
    private AuditoriaService auditoriaService;

    private SolicitudServiceImpl solicitudService;

    @BeforeEach
    void setUp() {
        solicitudService = new SolicitudServiceImpl(solicitudRepository, auditoriaService);
    }

    @Test
    void crearSolicitudServicioCalculaSlaYRegistraAuditoria() {
        Solicitud nueva = Solicitud.builder()
                .tipo(TipoSolicitud.SERVICIO)
                .prioridad(PrioridadSolicitud.ALTA)
                .asunto("Retraso en entrega de cerámica")
                .descripcion("El pedido #45 aún no registra entrega en Arequipa.")
                .usuarioId(5L)
                .solicitanteEmail("cliente@test.pe")
                .solicitanteRol("CLIENTE")
                .build();

        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        Solicitud creada = solicitudService.crear(nueva, "127.0.0.1", "corr-123");

        assertNotNull(creada.getCodigo());
        assertTrue(creada.getCodigo().startsWith("SRV-"));
        assertEquals(EstadoSolicitud.ABIERTA, creada.getEstado());
        assertNotNull(creada.getFechaLimiteSla());
        verify(auditoriaService, times(1)).registrar(any(RegistroAuditoria.class));
    }

    @Test
    void crearSolicitudAccesoSinRolSolicitadoLanzaError() {
        Solicitud acceso = Solicitud.builder()
                .tipo(TipoSolicitud.ACCESO)
                .asunto("Alta de rol")
                .descripcion("Solicito permisos")
                .build();

        assertThrows(BusinessException.class, () -> solicitudService.crear(acceso, "127.0.0.1", "corr-1"));
    }

    @Test
    void resolverDentroDeSlaMarcaSlaCumplidoTrue() {
        Solicitud existente = Solicitud.builder()
                .id(1L)
                .codigo("INF-2026-10001")
                .tipo(TipoSolicitud.INFORMACION)
                .prioridad(PrioridadSolicitud.MEDIA)
                .estado(EstadoSolicitud.EN_PROCESO)
                .fechaLimiteSla(LocalDateTime.now().plusHours(10))
                .build();

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        Solicitud resuelta = solicitudService.cambiarEstado(
                1L, EstadoSolicitud.RESUELTA, "Datos ARCO exportados correctamente",
                1L, "admin@arteandino.pe", "ADMINISTRADOR", "corr-2");

        assertEquals(EstadoSolicitud.RESUELTA, resuelta.getEstado());
        assertEquals(Boolean.TRUE, resuelta.getSlaCumplido());
        assertNotNull(resuelta.getFechaResolucion());
        verify(auditoriaService, times(1)).registrar(any(RegistroAuditoria.class));
    }

    @Test
    void resumenSlaCalculaMetricasCorrectamente() {
        Solicitud s1 = Solicitud.builder().tipo(TipoSolicitud.SERVICIO).estado(EstadoSolicitud.RESUELTA)
                .slaCumplido(true).fechaResolucion(LocalDateTime.now()).build();
        Solicitud s2 = Solicitud.builder().tipo(TipoSolicitud.ACCESO).estado(EstadoSolicitud.ABIERTA)
                .fechaLimiteSla(LocalDateTime.now().plusHours(4)).build();

        when(solicitudRepository.findAll()).thenReturn(List.of(s1, s2));

        Map<String, Object> resumen = solicitudService.obtenerResumenSla();

        assertEquals(2L, resumen.get("total"));
        assertEquals(1L, resumen.get("abiertas"));
        assertEquals(1L, resumen.get("resueltas"));
        assertEquals(100.0, resumen.get("porcentajeCumplimientoSla"));
    }
}
