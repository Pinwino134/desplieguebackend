package com.pinwino.microservice.pagosservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * "ventas-service" = spring.application.name de ventas-service, registrado en Eureka.
 */
@FeignClient(name = "ventasservice")
public interface PedidoClient {

    @PatchMapping("/api/v1/pedidos/{id}/confirmar-pago")
    Object confirmarPago(@PathVariable("id") Long pedidoId, @RequestParam("aprobado") boolean aprobado);
}
