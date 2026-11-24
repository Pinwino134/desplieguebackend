package com.pinwino.microservices.serviciosservice.model;

/**
 * Clasificación ITIL 4 de las solicitudes y tickets gestionados por Arte Andino:
 * - SERVICIO: soporte general, pedidos a medida, requerimientos operativos.
 * - INFORMACION: derechos ARCO, exportación de datos personales o consultas formales.
 * - ACCESO: alta como artesano, cambio de rol o solicitud de privilegios.
 * - INCIDENTE: interrupción no planificada o degradación de la calidad de un servicio.
 */
public enum TipoSolicitud {
    SERVICIO,
    INFORMACION,
    ACCESO,
    INCIDENTE
}
