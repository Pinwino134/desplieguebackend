package com.pinwino.microservices.serviciosservice.model;

/**
 * Niveles de atención y escalamiento del proceso de Gestión de Incidentes:
 * - N1_MESA_SERVICIO: Registro, clasificación, diagnóstico inicial y solución conocida.
 * - N2_ADMINISTRADOR: Operación de servicios, RBAC, configuración, despliegues y BD.
 * - N3_INGENIERIA: Código fuente, parches críticos, arquitectura y causa raíz profunda.
 * - GERENCIA_CRISIS: Escalamiento jerárquico para incidentes P1 / continuidad del negocio.
 */
public enum NivelEscalamiento {
    N1_MESA_SERVICIO,
    N2_ADMINISTRADOR,
    N3_INGENIERIA,
    GERENCIA_CRISIS
}
