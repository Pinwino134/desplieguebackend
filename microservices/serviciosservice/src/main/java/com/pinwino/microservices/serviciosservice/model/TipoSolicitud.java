package com.pinwino.microservices.serviciosservice.model;

/**
 * Clasificación ITIL/ISO-20000 de las solicitudes gestionadas por la plataforma:
 * - SERVICIO: soporte técnico, reclamos, pedidos a medida o incidencias.
 * - INFORMACION: solicitudes ARCO, exportación de datos personales o consultas formales.
 * - ACCESO: solicitud de alta como artesano, cambio de rol o permisos de módulo.
 */
public enum TipoSolicitud {
    SERVICIO,
    INFORMACION,
    ACCESO
}
