package com.pinwino.microservices.serviciosservice.model;

/**
 * Prioridad con tiempo objetivo de resolución (SLA) en horas.
 */
public enum PrioridadSolicitud {
    BAJA(72),
    MEDIA(24),
    ALTA(8),
    CRITICA(2);

    private final int horasSla;

    PrioridadSolicitud(int horasSla) {
        this.horasSla = horasSla;
    }

    public int getHorasSla() {
        return horasSla;
    }
}
