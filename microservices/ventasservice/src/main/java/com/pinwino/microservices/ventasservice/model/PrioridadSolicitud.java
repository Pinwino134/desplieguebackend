package com.pinwino.microservices.ventasservice.model;

/** Prioridad y su acuerdo de nivel de servicio (SLA): horas máximas para resolver. */
public enum PrioridadSolicitud {
    BAJA(72),
    MEDIA(24),
    ALTA(8),
    URGENTE(4);

    private final int horasSla;

    PrioridadSolicitud(int horasSla) {
        this.horasSla = horasSla;
    }

    public int getHorasSla() {
        return horasSla;
    }
}
