package com.pinwino.microservices.ventasservice.model;

public enum EstadoPedido {
    PENDIENTE_REVISION,
    COTIZADO,
    PENDIENTE_PAGO,
    CONFIRMADO,
    EN_PROCESO,
    ENVIADO,
    ENTREGADO,
    CANCELADO,
    RECHAZADO
}
