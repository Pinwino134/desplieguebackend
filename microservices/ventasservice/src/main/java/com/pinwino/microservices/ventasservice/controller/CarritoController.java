package com.pinwino.microservices.ventasservice.controller;

import com.pinwino.microservices.ventasservice.model.Carrito;
import com.pinwino.microservices.ventasservice.model.CarritoItem;
import com.pinwino.microservices.ventasservice.service.CarritoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carritos")
@RequiredArgsConstructor
public class CarritoController {
    private final CarritoService carritoService;

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<Carrito> obtenerDeCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(carritoService.obtenerOCrearPorCliente(clienteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Carrito> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(carritoService.buscarPorId(id));
    }

    @PostMapping("/{carritoId}/items")
    public ResponseEntity<CarritoItem> agregarItem(@PathVariable Long carritoId, @Valid @RequestBody CarritoItem item) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carritoService.agregarItem(carritoId, item));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CarritoItem> actualizarItem(@PathVariable Long itemId, @Valid @RequestBody CarritoItem item) {
        return ResponseEntity.ok(carritoService.actualizarItem(itemId, item));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> quitarItem(@PathVariable Long itemId) {
        carritoService.quitarItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{carritoId}/vaciar")
    public ResponseEntity<Void> vaciar(@PathVariable Long carritoId) {
        carritoService.vaciar(carritoId);
        return ResponseEntity.noContent().build();
    }
}
