package com.pinwino.microservice.gerenciaservice.service;

import com.pinwino.microservice.gerenciaservice.dto.CambioRequest;
import com.pinwino.microservice.gerenciaservice.exception.BusinessException;
import com.pinwino.microservice.gerenciaservice.exception.ForbiddenException;
import com.pinwino.microservice.gerenciaservice.exception.ResourceNotFoundException;
import com.pinwino.microservice.gerenciaservice.model.*;
import com.pinwino.microservice.gerenciaservice.repository.CambioHistorialRepository;
import com.pinwino.microservice.gerenciaservice.repository.CambioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Gestión de cambios (ITIL 4, habilitación de cambios).
 *
 * Roles:
 *  - ADMINISTRADOR: propone cambios, registra la implementación y los cancela.
 *  - GERENCIA: actúa como comité de cambios: evalúa, aprueba o rechaza, verifica y cierra.
 *
 * Reglas:
 *  - Cambio ESTÁNDAR: solo si el riesgo es BAJO; nace aprobado (preautorizado).
 *  - Cambio de EMERGENCIA: gerencia puede aprobarlo sin pasar por evaluación.
 *  - Separación de funciones: quien propone un cambio no puede aprobarlo.
 *  - Todo cambio exige plan de implementación y plan de reversa.
 */
@Service
@RequiredArgsConstructor
public class CambioService {

    public static final String ADMIN = "ADMINISTRADOR";
    public static final String GERENCIA = "GERENCIA";

    private final CambioRepository cambioRepository;
    private final CambioHistorialRepository historialRepository;

    // =========================================================
    // CONSULTAS
    // =========================================================

    public List<Cambio> listar(String estado, String tipo, String riesgo) {
        EstadoCambio e = vacio(estado) ? null : parse(EstadoCambio.class, estado, "Estado");
        TipoCambio t = vacio(tipo) ? null : parse(TipoCambio.class, tipo, "Tipo");
        NivelCambio r = vacio(riesgo) ? null : parse(NivelCambio.class, riesgo, "Riesgo");

        return cambioRepository.findAllByOrderByFechaCreacionDesc().stream()
                .filter(c -> e == null || c.getEstado() == e)
                .filter(c -> t == null || c.getTipo() == t)
                .filter(c -> r == null || c.getRiesgo() == r)
                .toList();
    }

    public Cambio obtener(Long id) {
        return buscar(id);
    }

    public List<CambioHistorial> historial(Long id) {
        buscar(id);
        return historialRepository.findByCambioIdOrderByFechaAscIdAsc(id);
    }

    // =========================================================
    // PROPONER
    // =========================================================

    @Transactional
    public Cambio crear(Long usuarioId, String tipoActor, CambioRequest req) {
        exigirStaff(tipoActor);

        String titulo = limpiar(req.getTitulo());
        String descripcion = limpiar(req.getDescripcion());
        String motivo = limpiar(req.getMotivo());
        String planImpl = limpiar(req.getPlanImplementacion());
        String planReversa = limpiar(req.getPlanReversa());

        if (titulo.isEmpty() || titulo.length() > 150) {
            throw new BusinessException("El título es obligatorio (máximo 150 caracteres)");
        }
        if (descripcion.isEmpty() || descripcion.length() > 2000) {
            throw new BusinessException("La descripción es obligatoria (máximo 2000 caracteres)");
        }
        if (motivo.isEmpty() || motivo.length() > 1000) {
            throw new BusinessException("El motivo es obligatorio (máximo 1000 caracteres)");
        }
        if (planImpl.isEmpty() || planImpl.length() > 2000) {
            throw new BusinessException("El plan de implementación es obligatorio (máximo 2000 caracteres)");
        }
        if (planReversa.isEmpty() || planReversa.length() > 2000) {
            throw new BusinessException("El plan de reversa es obligatorio: ¿cómo se vuelve atrás? (máximo 2000 caracteres)");
        }
        String planPruebas = limpiar(req.getPlanPruebas());
        if (planPruebas.length() > 1000) {
            throw new BusinessException("El plan de pruebas admite máximo 1000 caracteres");
        }

        TipoCambio tipo = parse(TipoCambio.class, req.getTipo(), "Tipo de cambio");
        ComponenteCambio componente = parse(ComponenteCambio.class, req.getComponente(), "Componente");
        NivelCambio impacto = parse(NivelCambio.class, req.getImpacto(), "Impacto");
        NivelCambio riesgo = parse(NivelCambio.class, req.getRiesgo(), "Riesgo");

        if (tipo == TipoCambio.ESTANDAR && riesgo != NivelCambio.BAJO) {
            throw new BusinessException("Un cambio estándar debe ser de riesgo BAJO; si no, propónlo como normal");
        }

        LocalDateTime ahora = LocalDateTime.now();
        boolean preautorizado = tipo == TipoCambio.ESTANDAR;

        Cambio c = cambioRepository.save(Cambio.builder()
                .titulo(titulo)
                .descripcion(descripcion)
                .tipo(tipo)
                .componente(componente)
                .impacto(impacto)
                .riesgo(riesgo)
                .motivo(motivo)
                .planImplementacion(planImpl)
                .planReversa(planReversa)
                .planPruebas(planPruebas.isEmpty() ? null : planPruebas)
                .fechaPlanificada(req.getFechaPlanificada())
                .solicitanteId(usuarioId)
                .solicitanteTipo(tipoActor)
                .estado(preautorizado ? EstadoCambio.APROBADO : EstadoCambio.PROPUESTO)
                .fechaCreacion(ahora)
                .fechaActualizacion(ahora)
                .build());

        registrar(c.getId(), usuarioId, tipoActor, "CREADO",
                "Cambio propuesto (" + tipo.getNombre() + ", riesgo " + riesgo + ", impacto " + impacto + ")");
        if (preautorizado) {
            registrar(c.getId(), usuarioId, tipoActor, "DECISION",
                    "Aprobado automáticamente: cambio estándar preautorizado");
        }
        return c;
    }

    // =========================================================
    // COMITÉ DE CAMBIOS (GERENCIA)
    // =========================================================

    @Transactional
    public Cambio evaluar(Long id, Long usuarioId, String tipoActor) {
        exigirGerencia(tipoActor);
        Cambio c = buscar(id);
        mover(c, EstadoCambio.EN_EVALUACION);
        c.setFechaActualizacion(LocalDateTime.now());
        registrar(id, usuarioId, tipoActor, "CAMBIO_ESTADO", "PROPUESTO → EN_EVALUACION");
        return cambioRepository.save(c);
    }

    @Transactional
    public Cambio decidir(Long id, Long usuarioId, String tipoActor, boolean aprobado, String motivo) {
        exigirGerencia(tipoActor);
        Cambio c = buscar(id);

        if (c.getSolicitanteId().equals(usuarioId)) {
            throw new BusinessException("Separación de funciones: no puedes decidir un cambio que tú mismo propusiste");
        }
        // La emergencia puede decidirse sin evaluación previa
        boolean puedeDecidir = c.getEstado() == EstadoCambio.EN_EVALUACION
                || (c.getEstado() == EstadoCambio.PROPUESTO && c.getTipo() == TipoCambio.EMERGENCIA);
        if (!puedeDecidir) {
            throw new BusinessException(
                    "Primero hay que pasar el cambio a evaluación (solo las emergencias se aprueban directo)");
        }

        String nota = limpiar(motivo);
        if (!aprobado && nota.isEmpty()) {
            throw new BusinessException("Al rechazar un cambio debes indicar el motivo");
        }
        if (nota.length() > 1000) {
            throw new BusinessException("El motivo admite máximo 1000 caracteres");
        }

        EstadoCambio anterior = c.getEstado();
        LocalDateTime ahora = LocalDateTime.now();
        c.setEstado(aprobado ? EstadoCambio.APROBADO : EstadoCambio.RECHAZADO);
        c.setAprobadorId(usuarioId);
        c.setFechaDecision(ahora);
        c.setMotivoDecision(nota.isEmpty() ? null : nota);
        c.setFechaActualizacion(ahora);

        registrar(id, usuarioId, tipoActor, "DECISION",
                (aprobado ? "APROBADO" : "RECHAZADO") + " (desde " + anterior + ")"
                        + (nota.isEmpty() ? "" : ": " + nota));
        return cambioRepository.save(c);
    }

    @Transactional
    public Cambio verificar(Long id, Long usuarioId, String tipoActor, String notas) {
        exigirGerencia(tipoActor);
        Cambio c = buscar(id);
        mover(c, EstadoCambio.VERIFICADO);
        c.setFechaActualizacion(LocalDateTime.now());
        registrar(id, usuarioId, tipoActor, "CAMBIO_ESTADO",
                "IMPLEMENTADO → VERIFICADO" + sufijo(notas));
        return cambioRepository.save(c);
    }

    @Transactional
    public Cambio cerrar(Long id, Long usuarioId, String tipoActor, String notas) {
        exigirGerencia(tipoActor);
        Cambio c = buscar(id);
        EstadoCambio anterior = c.getEstado();
        mover(c, EstadoCambio.CERRADO);
        LocalDateTime ahora = LocalDateTime.now();
        c.setFechaCierre(ahora);
        c.setFechaActualizacion(ahora);
        registrar(id, usuarioId, tipoActor, "CAMBIO_ESTADO",
                anterior + " → CERRADO" + sufijo(notas));
        return cambioRepository.save(c);
    }

    // =========================================================
    // EJECUCIÓN (ADMINISTRADOR)
    // =========================================================

    @Transactional
    public Cambio implementar(Long id, Long usuarioId, String tipoActor, String resultado, String notas) {
        if (!ADMIN.equalsIgnoreCase(tipoActor)) {
            throw new ForbiddenException("Solo un administrador registra la implementación");
        }
        Cambio c = buscar(id);
        ResultadoCambio r = parse(ResultadoCambio.class, resultado, "Resultado");
        String nota = limpiar(notas);
        if (nota.length() > 1000) {
            throw new BusinessException("Las notas admiten máximo 1000 caracteres");
        }

        mover(c, EstadoCambio.IMPLEMENTADO);
        LocalDateTime ahora = LocalDateTime.now();
        c.setImplementadorId(usuarioId);
        c.setFechaImplementacion(ahora);
        c.setResultado(r);
        c.setNotasImplementacion(nota.isEmpty() ? null : nota);
        c.setFechaActualizacion(ahora);

        registrar(id, usuarioId, tipoActor, "IMPLEMENTACION",
                "Implementado con resultado " + r + sufijo(nota));
        return cambioRepository.save(c);
    }

    // =========================================================
    // CANCELAR / COMENTAR
    // =========================================================

    @Transactional
    public Cambio cancelar(Long id, Long usuarioId, String tipoActor) {
        exigirStaff(tipoActor);
        Cambio c = buscar(id);

        boolean esDueno = c.getSolicitanteId().equals(usuarioId);
        if (!esDueno && !ADMIN.equalsIgnoreCase(tipoActor) && !GERENCIA.equalsIgnoreCase(tipoActor)) {
            throw new ForbiddenException("No puedes cancelar este cambio");
        }

        EstadoCambio anterior = c.getEstado();
        mover(c, EstadoCambio.CANCELADO);
        c.setFechaActualizacion(LocalDateTime.now());
        registrar(id, usuarioId, tipoActor, "CAMBIO_ESTADO", anterior + " → CANCELADO");
        return cambioRepository.save(c);
    }

    @Transactional
    public Cambio comentar(Long id, Long usuarioId, String tipoActor, String texto) {
        exigirStaff(tipoActor);
        Cambio c = buscar(id);

        String detalle = limpiar(texto);
        if (detalle.isEmpty() || detalle.length() > 2000) {
            throw new BusinessException("El comentario es obligatorio (máximo 2000 caracteres)");
        }

        registrar(id, usuarioId, tipoActor, "COMENTARIO", detalle);
        c.setFechaActualizacion(LocalDateTime.now());
        return cambioRepository.save(c);
    }

    // =========================================================
    // INDICADORES
    // =========================================================

    public Map<String, Object> resumen() {
        List<Cambio> todos = cambioRepository.findAll();
        LocalDateTime ahora = LocalDateTime.now();

        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (EstadoCambio e : EstadoCambio.values()) {
            porEstado.put(e.name(), todos.stream().filter(c -> c.getEstado() == e).count());
        }
        Map<String, Long> porTipo = new LinkedHashMap<>();
        for (TipoCambio t : TipoCambio.values()) {
            porTipo.put(t.name(), todos.stream().filter(c -> c.getTipo() == t).count());
        }
        Map<String, Long> porRiesgo = new LinkedHashMap<>();
        for (NivelCambio n : NivelCambio.values()) {
            porRiesgo.put(n.name(), todos.stream().filter(c -> c.getRiesgo() == n).count());
        }
        Map<String, Long> porComponente = new LinkedHashMap<>();
        for (ComponenteCambio comp : ComponenteCambio.values()) {
            porComponente.put(comp.name(), todos.stream().filter(c -> c.getComponente() == comp).count());
        }

        long pendientesAprobacion = todos.stream()
                .filter(c -> c.getEstado() == EstadoCambio.PROPUESTO
                        || c.getEstado() == EstadoCambio.EN_EVALUACION)
                .count();
        long porImplementar = todos.stream().filter(c -> c.getEstado() == EstadoCambio.APROBADO).count();

        List<Cambio> implementados = todos.stream()
                .filter(c -> c.getResultado() != null).toList();
        long exitosos = implementados.stream().filter(c -> c.getResultado() == ResultadoCambio.EXITOSO).count();
        long fallidos = implementados.stream().filter(c -> c.getResultado() == ResultadoCambio.FALLIDO).count();
        long revertidos = implementados.stream().filter(c -> c.getResultado() == ResultadoCambio.REVERTIDO).count();

        double tasaExito = implementados.isEmpty() ? 0 : exitosos * 100.0 / implementados.size();
        long emergencias = todos.stream().filter(c -> c.getTipo() == TipoCambio.EMERGENCIA).count();
        double pctEmergencia = todos.isEmpty() ? 0 : emergencias * 100.0 / todos.size();

        // Tiempo medio desde que se propone hasta que gerencia decide
        double horasDecision = todos.stream()
                .filter(c -> c.getAprobadorId() != null && c.getFechaDecision() != null)
                .mapToDouble(c -> Duration.between(c.getFechaCreacion(), c.getFechaDecision()).toMinutes() / 60.0)
                .average().orElse(0);

        List<Map<String, Object>> proximos = todos.stream()
                .filter(c -> c.getEstado() == EstadoCambio.APROBADO
                        && c.getFechaPlanificada() != null
                        && c.getFechaPlanificada().isAfter(ahora))
                .sorted(Comparator.comparing(Cambio::getFechaPlanificada))
                .limit(5)
                .map(c -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", c.getId());
                    m.put("codigo", c.getCodigo());
                    m.put("titulo", c.getTitulo());
                    m.put("fechaPlanificada", c.getFechaPlanificada());
                    return m;
                })
                .toList();

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("total", todos.size());
        r.put("abiertos", todos.stream().filter(c -> c.getEstado().abierto()).count());
        r.put("pendientesAprobacion", pendientesAprobacion);
        r.put("porImplementar", porImplementar);
        r.put("implementados", implementados.size());
        r.put("exitosos", exitosos);
        r.put("fallidos", fallidos);
        r.put("revertidos", revertidos);
        r.put("conDatosExito", !implementados.isEmpty());
        r.put("tasaExito", redondear(tasaExito));
        r.put("porcentajeEmergencia", redondear(pctEmergencia));
        r.put("horasPromedioDecision", redondear(horasDecision));
        r.put("proximos", proximos);
        r.put("porEstado", porEstado);
        r.put("porTipo", porTipo);
        r.put("porRiesgo", porRiesgo);
        r.put("porComponente", porComponente);
        return r;
    }

    // =========================================================
    // AYUDAS INTERNAS
    // =========================================================

    private Cambio buscar(Long id) {
        return cambioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cambio", id));
    }

    private void mover(Cambio c, EstadoCambio destino) {
        if (!c.getEstado().puedePasarA(destino)) {
            throw new BusinessException("No se puede pasar de " + c.getEstado() + " a " + destino);
        }
        c.setEstado(destino);
    }

    private void registrar(Long cambioId, Long autorId, String autorTipo, String evento, String detalle) {
        historialRepository.save(CambioHistorial.builder()
                .cambioId(cambioId)
                .autorId(autorId)
                .autorTipo(autorTipo)
                .evento(evento)
                .detalle(detalle)
                .fecha(LocalDateTime.now())
                .build());
    }

    private void exigirStaff(String tipoActor) {
        if (!ADMIN.equalsIgnoreCase(tipoActor) && !GERENCIA.equalsIgnoreCase(tipoActor)) {
            throw new ForbiddenException("Solo administración y gerencia gestionan cambios");
        }
    }

    private void exigirGerencia(String tipoActor) {
        if (!GERENCIA.equalsIgnoreCase(tipoActor)) {
            throw new ForbiddenException("Solo gerencia (comité de cambios) puede realizar esta acción");
        }
    }

    private boolean vacio(String s) {
        return s == null || s.isBlank();
    }

    private String limpiar(String s) {
        return s == null ? "" : s.trim();
    }

    private String sufijo(String notas) {
        String n = limpiar(notas);
        return n.isEmpty() ? "" : ": " + n;
    }

    private double redondear(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private <E extends Enum<E>> E parse(Class<E> tipo, String valor, String nombre) {
        try {
            return Enum.valueOf(tipo, limpiar(valor).toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(nombre + " no válido: " + valor);
        }
    }
}
