package com.pinwino.microservices.ventasservice.model;

/** Catálogo de solicitudes: cada tipo trae su prioridad (y por lo tanto su SLA) por defecto. */
public enum TipoSolicitud {
    CONSULTA_PEDIDO("Consulta sobre un pedido", PrioridadSolicitud.MEDIA),
    SOPORTE_TECNICO("Soporte técnico", PrioridadSolicitud.ALTA),
    CAMBIO_DE_DATOS("Cambio de datos de mi cuenta", PrioridadSolicitud.MEDIA),
    ALTA_ARTESANO("Activación de cuenta de artesano", PrioridadSolicitud.MEDIA),
    RECLAMO("Reclamo", PrioridadSolicitud.ALTA),
    SUGERENCIA("Sugerencia", PrioridadSolicitud.BAJA),
    OTRO("Otro", PrioridadSolicitud.BAJA);

    private final String nombre;
    private final PrioridadSolicitud prioridadPorDefecto;

    TipoSolicitud(String nombre, PrioridadSolicitud prioridadPorDefecto) {
        this.nombre = nombre;
        this.prioridadPorDefecto = prioridadPorDefecto;
    }

    public String getNombre() {
        return nombre;
    }

    public PrioridadSolicitud getPrioridadPorDefecto() {
        return prioridadPorDefecto;
    }
}
