package com.pinwino.microservices.serviciosservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Expone el Plan de Continuidad del Servicio (BCP) y Recuperación ante Desastres (DRP)
 * de Arte Andino: clasificación de servicios críticos, RTO, RPO, matriz de riesgos,
 * estrategia de alta disponibilidad y política de respaldos 3-2-1.
 */
@RestController
@RequestMapping("/api/v1/continuidad")
public class ContinuidadController {

    @GetMapping("/plan")
    public ResponseEntity<Map<String, Object>> obtenerPlanContinuidad() {
        List<Map<String, Object>> serviciosCriticos = List.of(
                servicio("apigateway", "Tier 1 - Crítico", "Punto único de entrada, JWT, RBAC y Rate Limiting", "15 min", "0 min (stateless)", "Balanceo vía Spring Cloud LoadBalancer + Healthchecks Actuator + restart: unless-stopped"),
                servicio("postgres", "Tier 1 - Crítico", "Persistencia de las 7 bases de datos del negocio", "30 min", "15 min (WAL/Backup)", "Volumen persistente + respaldo automatizado 3-2-1 (postgres/backup.sh) + restauración verificada (postgres/restore.sh)"),
                servicio("authservice", "Tier 1 - Crítico", "Autenticación, emisión JWT, bloqueo anti-fuerza bruta y roles", "15 min", "15 min", "Escalable horizontalmente en Eureka + HikariCP acotado + Healthcheck"),
                servicio("ventasservice", "Tier 1 - Crítico", "Catálogo comercial, carrito, pedidos y facturación comercial", "30 min", "15 min", "Timeouts Feign (3s/8s) + N+1 optimizado + Healthcheck"),
                servicio("pagosservice", "Tier 1 - Crítico", "Registro de pagos, comprobantes y reembolsos", "30 min", "5 min", "Integridad transaccional + no almacenamiento de PAN/CVV + respaldo diario"),
                servicio("productosservice", "Tier 2 - Alto", "Catálogo de artesanías, categorías, materiales y producción", "1 hora", "1 hora", "Caché Caffeine (60s) para absorber picos de lectura + Healthcheck"),
                servicio("logisticaservice", "Tier 2 - Alto", "Almacenes, inventario, proveedores, transportistas y envíos", "1 hora", "1 hora", "Aislamiento de BD logisticadb + seguimiento independiente"),
                servicio("configserver / discoveryserver", "Tier 2 - Alto", "Configuración centralizada y descubrimiento de servicios", "15 min", "0 min (Git como fuente)", "Perfil native empaquetado en imagen + arranque ordenado con depends_on service_healthy"),
                servicio("gerenciaservice", "Tier 3 - Medio", "KPIs, presupuestos, decisiones estratégicas y reportes", "4 horas", "24 horas", "Lectura asíncrona/bajo demanda sin bloquear ventas"),
                servicio("serviciosservice", "Tier 3 - Medio", "Mesa de servicio ITIL, eventos, incidentes y auditoría", "2 horas", "1 hora", "BD aislada serviciosdb + bitácora inmutable")
        );

        List<Map<String, Object>> matrizRiesgos = List.of(
                riesgo("R-01", "Caída o corrupción de PostgreSQL", "Media", "Crítico", "Backup 3-2-1 automatizado (postgres/backup.sh) y script de recuperación probado (postgres/restore.sh)"),
                riesgo("R-02", "Caída de VM en Oracle Cloud", "Baja", "Crítico", "Infraestructura como código (Docker Compose + GitHub Actions) permitiendo redespliegue completo en < 30 min"),
                riesgo("R-03", "Ataque de fuerza bruta o DDoS en Login/API", "Alta", "Alto", "RateLimitFilter por IP en Gateway (429) + bloqueo de cuenta por 3 intentos fallidos (423) + cabeceras OWASP"),
                riesgo("R-04", "Saturación de memoria JVM o fuga de conexiones", "Media", "Alto", "Límites de RAM por contenedor + JAVA_OPTS -XX:MaxRAMPercentage=75 + pool HikariCP máximo 10 + alertas Prometheus"),
                riesgo("R-05", "Despliegue defectuoso (regresión en producción)", "Media", "Alto", "CI obligatorio con pruebas unitarias/seguridad, smoke tests post-deploy y plan de rollback por commit en Git")
        );

        Map<String, Object> estrategiaRespaldos = new LinkedHashMap<>();
        estrategiaRespaldos.put("regla", "3-2-1 (3 copias, 2 medios distintos, 1 copia externa/off-site)");
        estrategiaRespaldos.put("respaldoDiario", "Dump lógico comprimido (.sql.gz) de las 7 BDs cada 24h a las 02:00 via cron + postgres/backup.sh");
        estrategiaRespaldos.put("retencionLocal", "14 días rotativos con depuración automática");
        estrategiaRespaldos.put("copiaExterna", "Sincronización cifrada a almacenamiento de objetos (Oracle Object Storage / nube externa)");
        estrategiaRespaldos.put("pruebaRestauracion", "Simulacro mensual con postgres/restore.sh verificando integridad referencial y cumplimiento de RTO/RPO");

        Map<String, Object> slaResumen = new LinkedHashMap<>();
        slaResumen.put("P1_CRITICA", Map.of("respuesta", "15 minutos", "resolucion", "2 horas", "escalamientoInicial", "N2_ADMINISTRADOR -> GERENCIA_CRISIS"));
        slaResumen.put("P2_ALTA", Map.of("respuesta", "30 minutos", "resolucion", "8 horas", "escalamientoInicial", "N1_MESA_SERVICIO -> N2_ADMINISTRADOR"));
        slaResumen.put("P3_MEDIA", Map.of("respuesta", "2 horas", "resolucion", "24 horas", "escalamientoInicial", "N1_MESA_SERVICIO"));
        slaResumen.put("P4_BAJA", Map.of("respuesta", "4 horas", "resolucion", "72 horas", "escalamientoInicial", "N1_MESA_SERVICIO"));

        Map<String, Object> plan = new LinkedHashMap<>();
        plan.put("disponibilidadObjetivo", "99.5% mensual");
        plan.put("rtoMaximoCritico", "30 minutos");
        plan.put("rpoMaximoCritico", "15 minutos");
        plan.put("serviciosCriticos", serviciosCriticos);
        plan.put("matrizRiesgos", matrizRiesgos);
        plan.put("estrategiaRespaldos", estrategiaRespaldos);
        plan.put("acuerdosSla", slaResumen);
        return ResponseEntity.ok(plan);
    }

    private Map<String, Object> servicio(String nombre, String tier, String funcion, String rto, String rpo, String altaDisponibilidad) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("servicio", nombre);
        m.put("tier", tier);
        m.put("funcion", funcion);
        m.put("rto", rto);
        m.put("rpo", rpo);
        m.put("altaDisponibilidad", altaDisponibilidad);
        return m;
    }

    private Map<String, Object> riesgo(String codigo, String escenario, String probabilidad, String impacto, String mitigacion) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("codigo", codigo);
        m.put("escenario", escenario);
        m.put("probabilidad", probabilidad);
        m.put("impacto", impacto);
        m.put("mitigacion", mitigacion);
        return m;
    }
}
