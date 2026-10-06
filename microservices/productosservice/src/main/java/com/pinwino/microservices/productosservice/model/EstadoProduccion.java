package com.pinwino.microservices.productosservice.model;

// Estados que puede tener una orden de producción.

public enum EstadoProduccion {

    PENDIENTE,

    INICIADO,

    EN_PROCESO,

    AVANZADO,

    TERMINADO
}