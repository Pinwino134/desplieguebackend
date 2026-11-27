package com.pinwino.microservices.serviciosservice.model;

/**
 * Prioridad ITIL (P1–P4) derivada de la Matriz Impacto × Urgencia,
 * con SLA formal de Primera Respuesta (minutos) y Resolución (horas).
 */
public enum PrioridadSolicitud {
    CRITICA("P1", 15, 2),
    ALTA("P2", 30, 8),
    MEDIA("P3", 120, 24),
    BAJA("P4", 240, 72);

    private final String codigoNivel;
    private final int minutosRespuestaSla;
    private final int horasSla;

    PrioridadSolicitud(String codigoNivel, int minutosRespuestaSla, int horasSla) {
        this.codigoNivel = codigoNivel;
        this.minutosRespuestaSla = minutosRespuestaSla;
        this.horasSla = horasSla;
    }

    public String getCodigoNivel() {
        return codigoNivel;
    }

    public int getMinutosRespuestaSla() {
        return minutosRespuestaSla;
    }

    public int getHorasSla() {
        return horasSla;
    }

    /**
     * Matriz oficial de priorización ITIL (Impacto × Urgencia):
     * - ALTO  × ALTA  = CRITICA (P1) | ALTO  × MEDIA = ALTA (P2) | ALTO  × BAJA = MEDIA (P3)
     * - MEDIO × ALTA  = ALTA (P2)    | MEDIO × MEDIA = MEDIA (P3)| MEDIO × BAJA = BAJA (P4)
     * - BAJO  × ALTA  = MEDIA (P3)   | BAJO  × MEDIA = BAJA (P4) | BAJO  × BAJA = BAJA (P4)
     */
    public static PrioridadSolicitud calcularPorMatriz(NivelImpacto impacto, NivelUrgencia urgencia) {
        if (impacto == null || urgencia == null) {
            return MEDIA;
        }
        return switch (impacto) {
            case ALTO -> switch (urgencia) {
                case ALTA -> CRITICA;
                case MEDIA -> ALTA;
                case BAJA -> MEDIA;
            };
            case MEDIO -> switch (urgencia) {
                case ALTA -> ALTA;
                case MEDIA -> MEDIA;
                case BAJA -> BAJA;
            };
            case BAJO -> switch (urgencia) {
                case ALTA -> MEDIA;
                case MEDIA, BAJA -> BAJA;
            };
        };
    }
}
