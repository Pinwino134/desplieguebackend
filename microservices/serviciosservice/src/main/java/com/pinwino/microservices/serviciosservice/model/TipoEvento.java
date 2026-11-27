package com.pinwino.microservices.serviciosservice.model;

/**
 * Clasificación ITIL 4 de eventos de monitoreo:
 * - INFORMATIVO: operación normal registrada para trazabilidad (ej. respaldo exitoso, deploy OK).
 * - ADVERTENCIA: acercamiento a umbral de capacidad o anomalía no bloqueante (ej. Heap > 75%, p95 > 400ms).
 * - EXCEPCION: falla o degradación activa que requiere atención inmediata o escalamiento a incidente.
 */
public enum TipoEvento {
    INFORMATIVO,
    ADVERTENCIA,
    EXCEPCION
}
