package com.pinwino.microservices.ventasservice.client;

import com.pinwino.microservices.ventasservice.client.dto.ProductoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Cliente Feign hacia productosservice.
 */
@FeignClient(name = "productosservice")
public interface ProductoClient {

    @GetMapping("/api/v1/productos/{id}")
    ProductoDTO buscarPorId(@PathVariable("id") Long id);

    @GetMapping("/api/v1/productos")
    List<ProductoDTO> listarPorArtesano(@RequestParam("artesanoId") Long artesanoId);

    @PostMapping("/api/v1/productos/{id}/stock")
    ProductoDTO ajustarStock(@PathVariable("id") Long id, @RequestParam("delta") Integer delta);
}
