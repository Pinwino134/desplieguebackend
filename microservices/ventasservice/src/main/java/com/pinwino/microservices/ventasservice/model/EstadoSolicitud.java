package com.pinwino.microservices.ventasservice.model;

/**
 * Ciclo de vida de una solicitud (ITIL: gestión de solicitudes de servicio).
 *
 * REGISTRADA -> EN_ATENCION -> (EN_ESPERA) -> RESUELTA -> CERRADA
 * Se puede CANCELAR mientras esté abierta, y reabrir una RESUELTA.
 */
public enum EstadoSolicitud {
    REGISTRADA,
    EN_ATENCION,
    EN_ESPERA,   // esperando una respuesta del usuario
    RESUELTA,
    CERRADA,
    CANCELADA;

    /** Abierta = todavía requiere trabajo o respuesta. */
    public boolean abierta() {
        return this == REGISTRADA || this == EN_ATENCION || this == EN_ESPERA;
    }

    public boolean puedePasarA(EstadoSolicitud destino) {
        return switch (this) {
            case REGISTRADA -> destino == EN_ATENCION || destino == CANCELADA;
            case EN_ATENCION -> destino == EN_ESPERA || destino == RESUELTA || destino == CANCELADA;
            case EN_ESPERA -> destino == EN_ATENCION || destino == RESUELTA || destino == CANCELADA;
            case RESUELTA -> destino == CERRADA || destino == EN_ATENCION;
            case CERRADA, CANCELADA -> false;
        };
    }
}
