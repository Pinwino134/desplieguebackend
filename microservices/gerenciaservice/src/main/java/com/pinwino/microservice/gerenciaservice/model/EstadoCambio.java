package com.pinwino.microservice.gerenciaservice.model;

/**
 * Ciclo de vida de un cambio:
 * PROPUESTO -> EN_EVALUACION -> APROBADO -> IMPLEMENTADO -> VERIFICADO -> CERRADO
 * (RECHAZADO y CANCELADO son finales; un cambio de emergencia puede pasar de
 * PROPUESTO directo a APROBADO).
 */
public enum EstadoCambio {
    PROPUESTO,
    EN_EVALUACION,
    APROBADO,
    RECHAZADO,
    IMPLEMENTADO,
    VERIFICADO,
    CERRADO,
    CANCELADO;

    public boolean abierto() {
        return this != RECHAZADO && this != CERRADO && this != CANCELADO;
    }

    public boolean puedePasarA(EstadoCambio destino) {
        return switch (this) {
            case PROPUESTO -> destino == EN_EVALUACION || destino == APROBADO
                    || destino == CANCELADO;
            case EN_EVALUACION -> destino == APROBADO || destino == RECHAZADO
                    || destino == CANCELADO;
            case APROBADO -> destino == IMPLEMENTADO || destino == CANCELADO;
            case IMPLEMENTADO -> destino == VERIFICADO || destino == CERRADO;
            case VERIFICADO -> destino == CERRADO;
            case RECHAZADO, CERRADO, CANCELADO -> false;
        };
    }
}
