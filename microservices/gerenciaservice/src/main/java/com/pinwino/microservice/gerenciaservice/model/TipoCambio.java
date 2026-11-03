package com.pinwino.microservice.gerenciaservice.model;

/** Tipos de cambio (ITIL 4, habilitación de cambios). */
public enum TipoCambio {
    ESTANDAR("Estándar (preautorizado)"),   // bajo riesgo, repetible: se aprueba solo
    NORMAL("Normal"),                        // se evalúa y aprueba por gerencia
    EMERGENCIA("Emergencia");                // urgente: gerencia puede aprobarlo sin evaluación previa

    private final String nombre;

    TipoCambio(String nombre) { this.nombre = nombre; }

    public String getNombre() { return nombre; }
}
