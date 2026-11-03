package com.pinwino.microservices.ventasservice.service;

import com.pinwino.microservices.ventasservice.exception.BusinessException;
import com.pinwino.microservices.ventasservice.exception.ForbiddenException;
import com.pinwino.microservices.ventasservice.exception.ResourceNotFoundException;
import com.pinwino.microservices.ventasservice.model.*;
import com.pinwino.microservices.ventasservice.repository.SolicitudHistorialRepository;
import com.pinwino.microservices.ventasservice.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Gestión de solicitudes de servicio (ITIL):
 *  - catálogo de tipos con prioridad y SLA por defecto
 *  - ciclo de vida con transiciones válidas
 *  - bitácora de cada solicitud
 *  - indicadores: abiertas, vencidas, cumplimiento de SLA, tiempo promedio de resolución
 *
 * Permisos: el dueño ve y comenta lo suyo; ADMINISTRADOR gestiona todo;
 * GERENCIA solo consulta.
 */
@Service
@RequiredArgsConstructor
public class SolicitudService {

    public static final String ADMIN = "ADMINISTRADOR";
    public static final String GERENCIA = "GERENCIA";

    private final SolicitudRepository solicitudRepository;
    private final SolicitudHistorialRepository historialRepository;

    // =========================================================
    // CONSULTAS
    // =========================================================

    public List<Map<String, Object>> catalogo() {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (TipoSolicitud t : TipoSolicitud.values()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("codigo", t.name());
            m.put("nombre", t.getNombre());
            m.put("prioridadPorDefecto", t.getPrioridadPorDefecto().name());
            m.put("horasSla", t.getPrioridadPorDefecto().getHorasSla());
            lista.add(m);
        }
        return lista;
    }

    public List<Solicitud> listarMias(Long usuarioId) {
        return solicitudRepository.findBySolicitanteIdOrderByFechaCreacionDesc(usuarioId);
    }

    public List<Solicitud> listarTodas(String estado, String tipo, String prioridad) {
        EstadoSolicitud e = estado == null || estado.isBlank() ? null : parseEstado(estado);
        TipoSolicitud t = tipo == null || tipo.isBlank() ? null : parseTipo(tipo);
        PrioridadSolicitud p = prioridad == null || prioridad.isBlank() ? null : parsePrioridad(prioridad);

        return solicitudRepository.findAllByOrderByFechaCreacionDesc().stream()
                .filter(s -> e == null || s.getEstado() == e)
                .filter(s -> t == null || s.getTipo() == t)
                .filter(s -> p == null || s.getPrioridad() == p)
                .toList();
    }

    public Solicitud obtener(Long id, Long usuarioId, String tipoActor) {
        Solicitud s = buscar(id);
        if (!puedeVer(s, usuarioId, tipoActor)) {
            throw new ForbiddenException("No tienes acceso a esta solicitud");
        }
        return s;
    }

    public List<SolicitudHistorial> historial(Long id, Long usuarioId, String tipoActor) {
        obtener(id, usuarioId, tipoActor); // valida acceso
        return historialRepository.findBySolicitudIdOrderByFechaAscIdAsc(id);
    }

    // =========================================================
    // ACCIONES DEL USUARIO
    // =========================================================

    @Transactional
    public Solicitud crear(Long usuarioId, String tipoActor, String tipo,
                           String asunto, String descripcion, Long pedidoId) {

        TipoSolicitud t = parseTipo(tipo);
        String a = limpiar(asunto);
        String d = limpiar(descripcion);

        if (a.isEmpty() || a.length() > 150) {
            throw new BusinessException("El asunto es obligatorio (máximo 150 caracteres)");
        }
        if (d.isEmpty() || d.length() > 2000) {
            throw new BusinessException("La descripción es obligatoria (máximo 2000 caracteres)");
        }

        PrioridadSolicitud prioridad = t.getPrioridadPorDefecto();
        LocalDateTime ahora = LocalDateTime.now();

        Solicitud s = solicitudRepository.save(Solicitud.builder()
                .solicitanteId(usuarioId)
                .solicitanteTipo(tipoActor)
                .tipo(t)
                .prioridad(prioridad)
                .estado(EstadoSolicitud.REGISTRADA)
                .asunto(a)
                .descripcion(d)
                .pedidoId(pedidoId)
                .fechaCreacion(ahora)
                .fechaActualizacion(ahora)
                .fechaLimite(ahora.plusHours(prioridad.getHorasSla()))
                .build());

        registrar(s.getId(), usuarioId, tipoActor, "CREADA",
                "Solicitud registrada: " + t.getNombre());
        return s;
    }

    @Transactional
    public Solicitud comentar(Long id, Long usuarioId, String tipoActor, String texto) {
        Solicitud s = buscar(id);
        boolean esDueno = s.getSolicitanteId().equals(usuarioId);

        if (!esAdmin(tipoActor) && !esDueno) {
            throw new ForbiddenException("No puedes comentar esta solicitud");
        }

        String detalle = limpiar(texto);
        if (detalle.isEmpty() || detalle.length() > 2000) {
            throw new BusinessException("El comentario es obligatorio (máximo 2000 caracteres)");
        }
        if (s.getEstado() == EstadoSolicitud.CERRADA || s.getEstado() == EstadoSolicitud.CANCELADA) {
            throw new BusinessException("La solicitud está " + s.getEstado() + " y ya no admite comentarios");
        }

        LocalDateTime ahora = LocalDateTime.now();
        registrar(id, usuarioId, tipoActor, "COMENTARIO", detalle);

        // Si el usuario responde, la solicitud vuelve a atención (o se reabre si estaba resuelta)
        if (esDueno && !esAdmin(tipoActor)
                && (s.getEstado() == EstadoSolicitud.EN_ESPERA || s.getEstado() == EstadoSolicitud.RESUELTA)) {
            EstadoSolicitud anterior = s.getEstado();
            s.setEstado(EstadoSolicitud.EN_ATENCION);
            if (anterior == EstadoSolicitud.RESUELTA) {
                s.setFechaResolucion(null);
            }
            registrar(id, usuarioId, tipoActor, "CAMBIO_ESTADO",
                    anterior + " → EN_ATENCION (respuesta del usuario)");
        }

        s.setFechaActualizacion(ahora);
        return solicitudRepository.save(s);
    }

    @Transactional
    public Solicitud cancelar(Long id, Long usuarioId, String tipoActor) {
        Solicitud s = buscar(id);

        if (!esAdmin(tipoActor) && !s.getSolicitanteId().equals(usuarioId)) {
            throw new ForbiddenException("Solo quien la creó o un administrador puede cancelarla");
        }
        if (!s.getEstado().puedePasarA(EstadoSolicitud.CANCELADA)) {
            throw new BusinessException("No se puede cancelar una solicitud en estado " + s.getEstado());
        }

        EstadoSolicitud anterior = s.getEstado();
        s.setEstado(EstadoSolicitud.CANCELADA);
        s.setFechaActualizacion(LocalDateTime.now());
        registrar(id, usuarioId, tipoActor, "CAMBIO_ESTADO", anterior + " → CANCELADA");
        return solicitudRepository.save(s);
    }

    // =========================================================
    // ACCIONES DEL ADMINISTRADOR
    // =========================================================

    @Transactional
    public Solicitud cambiarEstado(Long id, Long adminId, String tipoActor,
                                   String nuevoEstado, String comentario) {
        exigirAdmin(tipoActor);

        EstadoSolicitud destino = parseEstado(nuevoEstado);
        Solicitud s = buscar(id);
        EstadoSolicitud anterior = s.getEstado();

        if (!anterior.puedePasarA(destino)) {
            throw new BusinessException("No se puede pasar de " + anterior + " a " + destino);
        }

        LocalDateTime ahora = LocalDateTime.now();
        s.setEstado(destino);
        s.setFechaActualizacion(ahora);

        if (destino == EstadoSolicitud.RESUELTA) {
            s.setFechaResolucion(ahora);
        }
        if (destino == EstadoSolicitud.EN_ATENCION && anterior == EstadoSolicitud.RESUELTA) {
            s.setFechaResolucion(null); // reapertura
        }
        if (destino == EstadoSolicitud.EN_ATENCION && s.getAsignadoA() == null) {
            s.setAsignadoA(adminId);
        }

        String nota = limpiar(comentario);
        registrar(id, adminId, tipoActor, "CAMBIO_ESTADO",
                anterior + " → " + destino + (nota.isEmpty() ? "" : ": " + nota));
        return solicitudRepository.save(s);
    }

    /** asignadoA == null significa "asignármela a mí". */
    @Transactional
    public Solicitud asignar(Long id, Long adminId, String tipoActor, Long asignadoA) {
        exigirAdmin(tipoActor);

        Solicitud s = buscar(id);
        if (!s.getEstado().abierta()) {
            throw new BusinessException("Solo se pueden asignar solicitudes abiertas");
        }

        Long responsable = asignadoA != null ? asignadoA : adminId;
        s.setAsignadoA(responsable);
        s.setFechaActualizacion(LocalDateTime.now());
        registrar(id, adminId, tipoActor, "ASIGNADA", "Asignada al usuario #" + responsable);

        if (s.getEstado() == EstadoSolicitud.REGISTRADA) {
            s.setEstado(EstadoSolicitud.EN_ATENCION);
            registrar(id, adminId, tipoActor, "CAMBIO_ESTADO", "REGISTRADA → EN_ATENCION");
        }
        return solicitudRepository.save(s);
    }

    @Transactional
    public Solicitud cambiarPrioridad(Long id, Long adminId, String tipoActor, String prioridad) {
        exigirAdmin(tipoActor);

        PrioridadSolicitud nueva = parsePrioridad(prioridad);
        Solicitud s = buscar(id);
        if (!s.getEstado().abierta()) {
            throw new BusinessException("Solo se puede reclasificar una solicitud abierta");
        }

        PrioridadSolicitud anterior = s.getPrioridad();
        s.setPrioridad(nueva);
        // El plazo se recalcula desde que se creó la solicitud
        s.setFechaLimite(s.getFechaCreacion().plusHours(nueva.getHorasSla()));
        s.setFechaActualizacion(LocalDateTime.now());

        registrar(id, adminId, tipoActor, "CAMBIO_PRIORIDAD",
                anterior + " → " + nueva + " (SLA: " + nueva.getHorasSla() + " h)");
        return solicitudRepository.save(s);
    }

    // =========================================================
    // INDICADORES (administrador y gerencia)
    // =========================================================

    public Map<String, Object> resumen() {
        List<Solicitud> todas = solicitudRepository.findAll();

        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (EstadoSolicitud e : EstadoSolicitud.values()) {
            porEstado.put(e.name(), todas.stream().filter(s -> s.getEstado() == e).count());
        }

        Map<String, Long> porTipo = new LinkedHashMap<>();
        for (TipoSolicitud t : TipoSolicitud.values()) {
            porTipo.put(t.name(), todas.stream().filter(s -> s.getTipo() == t).count());
        }

        Map<String, Long> porPrioridad = new LinkedHashMap<>();
        for (PrioridadSolicitud p : PrioridadSolicitud.values()) {
            porPrioridad.put(p.name(), todas.stream().filter(s -> s.getPrioridad() == p).count());
        }

        long abiertas = todas.stream().filter(s -> s.getEstado().abierta()).count();
        long vencidas = todas.stream().filter(Solicitud::isVencida).count();
        long sinAsignar = todas.stream()
                .filter(s -> s.getEstado().abierta() && s.getAsignadoA() == null).count();

        // Solicitudes ya resueltas (o cerradas) con su fecha de resolución
        List<Solicitud> resueltas = todas.stream()
                .filter(s -> s.getFechaResolucion() != null
                        && (s.getEstado() == EstadoSolicitud.RESUELTA
                            || s.getEstado() == EstadoSolicitud.CERRADA))
                .toList();

        long enPlazo = resueltas.stream()
                .filter(s -> !s.getFechaResolucion().isAfter(s.getFechaLimite()))
                .count();

        double cumplimiento = resueltas.isEmpty() ? 0 : enPlazo * 100.0 / resueltas.size();
        double horasPromedio = resueltas.stream()
                .mapToDouble(s -> Duration.between(s.getFechaCreacion(), s.getFechaResolucion()).toMinutes() / 60.0)
                .average().orElse(0);

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("total", todas.size());
        r.put("abiertas", abiertas);
        r.put("vencidas", vencidas);
        r.put("sinAsignar", sinAsignar);
        r.put("resueltas", resueltas.size());
        r.put("conDatosSla", !resueltas.isEmpty());
        r.put("cumplimientoSla", redondear(cumplimiento));
        r.put("tiempoPromedioResolucionHoras", redondear(horasPromedio));
        r.put("porEstado", porEstado);
        r.put("porTipo", porTipo);
        r.put("porPrioridad", porPrioridad);
        return r;
    }

    // =========================================================
    // AYUDAS INTERNAS
    // =========================================================

    private Solicitud buscar(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud", id));
    }

    private void registrar(Long solicitudId, Long autorId, String autorTipo, String evento, String detalle) {
        historialRepository.save(SolicitudHistorial.builder()
                .solicitudId(solicitudId)
                .autorId(autorId)
                .autorTipo(autorTipo)
                .evento(evento)
                .detalle(detalle)
                .fecha(LocalDateTime.now())
                .build());
    }

    private boolean puedeVer(Solicitud s, Long usuarioId, String tipoActor) {
        return esAdmin(tipoActor) || GERENCIA.equalsIgnoreCase(tipoActor)
                || s.getSolicitanteId().equals(usuarioId);
    }

    private boolean esAdmin(String tipoActor) {
        return ADMIN.equalsIgnoreCase(tipoActor);
    }

    private void exigirAdmin(String tipoActor) {
        if (!esAdmin(tipoActor)) {
            throw new ForbiddenException("Solo un administrador puede realizar esta acción");
        }
    }

    private String limpiar(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private double redondear(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }

    private TipoSolicitud parseTipo(String valor) {
        try {
            return TipoSolicitud.valueOf(limpiar(valor).toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Tipo de solicitud no válido: " + valor);
        }
    }

    private EstadoSolicitud parseEstado(String valor) {
        try {
            return EstadoSolicitud.valueOf(limpiar(valor).toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Estado no válido: " + valor);
        }
    }

    private PrioridadSolicitud parsePrioridad(String valor) {
        try {
            return PrioridadSolicitud.valueOf(limpiar(valor).toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Prioridad no válida: " + valor);
        }
    }
}
