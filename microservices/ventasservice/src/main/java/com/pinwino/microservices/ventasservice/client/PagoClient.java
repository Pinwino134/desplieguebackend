package com.pinwino.microservices.ventasservice.client;

import com.pinwino.microservices.ventasservice.client.dto.PagoResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "pagosservice")
public interface PagoClient {

    @PostMapping("/api/v1/pagos")
    PagoResponseDTO procesar(@RequestParam("pedidoId") Long pedidoId,
                              @RequestParam("clienteId") Long clienteId,
                              @RequestParam(value = "metodoPagoId", required = false) Long metodoPagoId,
                              @RequestParam("monto") BigDecimal monto);
}
