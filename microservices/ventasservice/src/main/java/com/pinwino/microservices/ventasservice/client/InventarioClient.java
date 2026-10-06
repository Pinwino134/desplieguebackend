package com.pinwino.microservices.ventasservice.client;

import com.pinwino.microservices.ventasservice.client.dto.InventarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
	    name = "logisticaservice",
	    contextId = "inventarioClient"
)
public interface InventarioClient {

    @GetMapping("/api/v1/inventario")
    List<InventarioDTO> listarPorProducto(@RequestParam("productoId") Long productoId);
}
