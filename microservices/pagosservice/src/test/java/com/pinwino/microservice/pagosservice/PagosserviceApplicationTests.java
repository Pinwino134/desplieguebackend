package com.pinwino.microservice.pagosservice;

import com.pinwino.microservice.pagosservice.model.EstadoPago;
import com.pinwino.microservice.pagosservice.model.Pago;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PagosserviceApplicationTests {

    @Test
    void modeloPagoMantieneIntegridadDeEstadoYMonto() {
        Pago pago = new Pago();
        pago.setPedidoId(10L);
        pago.setClienteId(2L);
        pago.setMonto(new BigDecimal("350.00"));
        pago.setEstado(EstadoPago.PENDIENTE);

        assertEquals(EstadoPago.PENDIENTE, pago.getEstado());
        assertEquals(0, new BigDecimal("350.00").compareTo(pago.getMonto()));
    }
}
