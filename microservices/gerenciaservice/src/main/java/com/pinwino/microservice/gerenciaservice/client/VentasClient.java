package com.pinwino.microservice.gerenciaservice.client;

import com.pinwino.microservice.gerenciaservice.client.dto.PedidoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "ventasservice")
public interface VentasClient {

    @GetMapping("/api/v1/pedidos")
    List<PedidoDTO> listarPorEstado(@RequestParam("estado") String estado);
}
