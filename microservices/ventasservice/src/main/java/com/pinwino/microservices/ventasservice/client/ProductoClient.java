package com.pinwino.microservices.ventasservice.client;

import com.pinwino.microservices.ventasservice.client.dto.ProductoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * "productos-service" es el mismo valor que spring.application.name en
 * productos-service/application.yml. Feign + Eureka resuelven ese nombre
 * a la instancia real (IP:puerto) registrada en el discovery server.
 */
@FeignClient(name = "productosservice")
public interface ProductoClient {

    @GetMapping("/api/v1/productos/{id}")
    ProductoDTO buscarPorId(@PathVariable("id") Long id);
}
