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
    void crearIncidenteConMatrizImpactoUrgenciaCalculaPrioridadP1YNivelN2() {
        Solicitud incidente = Solicitud.builder()
                .tipo(TipoSolicitud.INCIDENTE)
                .impacto(NivelImpacto.ALTO)
                .urgencia(NivelUrgencia.ALTA)
                .servicioAfectado("pagosservice")
                .asunto("Caída de pasarela de pagos")
                .descripcion("Error 503 al procesar pagos de clientes.")
                .usuarioId(5L)
                .solicitanteEmail("cliente@test.pe")
                .solicitanteRol("CLIENTE")
                .build();

        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        Solicitud creada = solicitudService.crear(incidente, "127.0.0.1", "corr-123");

        assertTrue(creada.getCodigo().startsWith("INC-"));
        assertEquals(PrioridadSolicitud.CRITICA, creada.getPrioridad());
        assertEquals(NivelEscalamiento.N2_ADMINISTRADOR, creada.getNivelEscalamiento());
        assertEquals(EstadoSolicitud.ABIERTA, creada.getEstado());
        assertNotNull(creada.getFechaLimiteRespuestaSla());
        assertNotNull(creada.getFechaLimiteSla());
        verify(auditoriaService, times(1)).registrar(any(RegistroAuditoria.class));
    }

    @Test
    void matrizPriorizacionEvaluaTodosLosCuadrantesCorrectamente() {
        assertEquals(PrioridadSolicitud.CRITICA, PrioridadSolicitud.calcularPorMatriz(NivelImpacto.ALTO, NivelUrgencia.ALTA));
        assertEquals(PrioridadSolicitud.ALTA, PrioridadSolicitud.calcularPorMatriz(NivelImpacto.ALTO, NivelUrgencia.MEDIA));
        assertEquals(PrioridadSolicitud.MEDIA, PrioridadSolicitud.calcularPorMatriz(NivelImpacto.MEDIO, NivelUrgencia.MEDIA));
        assertEquals(PrioridadSolicitud.BAJA, PrioridadSolicitud.calcularPorMatriz(NivelImpacto.BAJO, NivelUrgencia.BAJA));
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
    void escalarIncidenteAN3RegistraPrimeraRespuestaYAuditoria() {
        Solicitud existente = Solicitud.builder()
                .id(2L)
                .codigo("INC-2026-20001")
                .tipo(TipoSolicitud.INCIDENTE)
                .prioridad(PrioridadSolicitud.ALTA)
                .estado(EstadoSolicitud.ABIERTA)
                .nivelEscalamiento(NivelEscalamiento.N1_MESA_SERVICIO)
                .fechaLimiteRespuestaSla(LocalDateTime.now().plusMinutes(25))
                .build();

        when(solicitudRepository.findById(2L)).thenReturn(Optional.of(existente));
        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        Solicitud escalada = solicitudService.escalar(
                2L, NivelEscalamiento.N3_INGENIERIA, "Requiere parche en Feign client",
                1L, "admin@arteandino.pe", "ADMINISTRADOR", "corr-esc");

        assertEquals(NivelEscalamiento.N3_INGENIERIA, escalada.getNivelEscalamiento());
        assertEquals(EstadoSolicitud.EN_PROCESO, escalada.getEstado());
        assertEquals(Boolean.TRUE, escalada.getSlaRespuestaCumplido());
        verify(auditoriaService, times(1)).registrar(any(RegistroAuditoria.class));
    }

    @Test
    void resolverDentroDeSlaGuardaWorkaroundCausaRaizYSlaCumplido() {
        Solicitud existente = Solicitud.builder()
                .id(1L)
                .codigo("INF-2026-10001")
                .tipo(TipoSolicitud.INFORMACION)
                .prioridad(PrioridadSolicitud.MEDIA)
                .estado(EstadoSolicitud.EN_PROCESO)
                .nivelEscalamiento(NivelEscalamiento.N1_MESA_SERVICIO)
                .fechaLimiteRespuestaSla(LocalDateTime.now().plusHours(1))
                .fechaLimiteSla(LocalDateTime.now().plusHours(10))
                .build();

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        Solicitud resuelta = solicitudService.cambiarEstado(
                1L, EstadoSolicitud.RESUELTA,
                "Datos ARCO exportados correctamente",
                "Entrega temporal por correo cifrado",
                "Solicitud periódica de transparencia",
                1L, "admin@arteandino.pe", "ADMINISTRADOR", "corr-2");

        assertEquals(EstadoSolicitud.RESUELTA, resuelta.getEstado());
        assertEquals(Boolean.TRUE, resuelta.getSlaCumplido());
        assertEquals("Entrega temporal por correo cifrado", resuelta.getSolucionTemporal());
        assertEquals("Solicitud periódica de transparencia", resuelta.getCausaRaiz());
        assertNotNull(resuelta.getFechaResolucion());
        verify(auditoriaService, times(1)).registrar(any(RegistroAuditoria.class));
    }

    @Test
    void resumenSlaCalculaMetricasPorTipoPrioridadYEscalamiento() {
        Solicitud s1 = Solicitud.builder().tipo(TipoSolicitud.INCIDENTE).prioridad(PrioridadSolicitud.CRITICA)
                .nivelEscalamiento(NivelEscalamiento.N2_ADMINISTRADOR).estado(EstadoSolicitud.RESUELTA)
                .slaCumplido(true).slaRespuestaCumplido(true)
                .fechaPrimeraRespuesta(LocalDateTime.now()).fechaResolucion(LocalDateTime.now()).build();
        Solicitud s2 = Solicitud.builder().tipo(TipoSolicitud.ACCESO).prioridad(PrioridadSolicitud.MEDIA)
                .nivelEscalamiento(NivelEscalamiento.N1_MESA_SERVICIO).estado(EstadoSolicitud.ABIERTA)
                .fechaLimiteSla(LocalDateTime.now().plusHours(4)).build();

        when(solicitudRepository.findAll()).thenReturn(List.of(s1, s2));

        Map<String, Object> resumen = solicitudService.obtenerResumenSla();

        assertEquals(2L, resumen.get("total"));
        assertEquals(1L, resumen.get("abiertas"));
        assertEquals(1L, resumen.get("resueltas"));
        assertEquals(100.0, resumen.get("porcentajeCumplimientoSla"));
        assertEquals(100.0, resumen.get("porcentajeCumplimientoSlaRespuesta"));
    }
}
