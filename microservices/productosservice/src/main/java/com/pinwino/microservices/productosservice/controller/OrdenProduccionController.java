package com.pinwino.microservices.productosservice.controller;

import com.pinwino.microservices.productosservice.dto.OrdenProduccionDTO;
import com.pinwino.microservices.productosservice.model.EstadoProduccion;
import com.pinwino.microservices.productosservice.model.OrdenProduccion;
import com.pinwino.microservices.productosservice.service.OrdenProduccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/producciones")
@RequiredArgsConstructor
public class OrdenProduccionController {

    private final OrdenProduccionService ordenProduccionService;

    // Lista las producciones de un artesano.
    @GetMapping
    public ResponseEntity<List<OrdenProduccionDTO>> listar(
            @RequestParam Long artesanoId) {

        List<OrdenProduccion> producciones =
                ordenProduccionService.listarPorArtesano(artesanoId);

        List<OrdenProduccionDTO> respuesta = producciones.stream()
                .map(orden -> new OrdenProduccionDTO(
                        orden.getId(),
                        orden.getPedidoId(),
                        orden.getProducto().getId(),
                        orden.getProducto().getNombre(),
                        orden.getCantidad(),
                        orden.getFechaInicio(),
                        orden.getAvance(),
                        orden.getEstado()
                ))
                .toList();

        return ResponseEntity.ok(respuesta);
    }

    // Busca una producción por su ID.
    @GetMapping("/{id}")
    public ResponseEntity<OrdenProduccionDTO> buscarPorId(
            @PathVariable Long id) {

        OrdenProduccion orden =
                ordenProduccionService.buscarPorId(id);

        OrdenProduccionDTO respuesta = new OrdenProduccionDTO(
                orden.getId(),
                orden.getPedidoId(),
                orden.getProducto().getId(),
                orden.getProducto().getNombre(),
                orden.getCantidad(),
                orden.getFechaInicio(),
                orden.getAvance(),
                orden.getEstado()
        );

        return ResponseEntity.ok(respuesta);
    }

    // Crea una nueva orden de producción.
    @PostMapping
    public ResponseEntity<OrdenProduccion> crear(
            @RequestParam Long productoId,
            @Valid @RequestBody OrdenProduccion ordenProduccion) {

        OrdenProduccion creada =
                ordenProduccionService.crear(
                        productoId,
                        ordenProduccion
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creada);
    }

    // Actualiza el porcentaje de avance.
    @PatchMapping("/{id}/avance")
    public ResponseEntity<OrdenProduccion> actualizarAvance(
            @PathVariable Long id,
            @RequestParam Integer avance) {

        return ResponseEntity.ok(
                ordenProduccionService.actualizarAvance(
                        id,
                        avance
                )
        );
    }

    // Cambia el estado de la producción.
    @PatchMapping("/{id}/estado")
    public ResponseEntity<OrdenProduccion> cambiarEstado(
            @PathVariable Long id,
            @RequestParam EstadoProduccion estado) {

        return ResponseEntity.ok(
                ordenProduccionService.cambiarEstado(
                        id,
                        estado
                )
        );
    }
}