package com.pinwino.microservice.pagosservice.controller;

import com.pinwino.microservice.pagosservice.model.ClienteMetodoPago;
import com.pinwino.microservice.pagosservice.service.ClienteMetodoPagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/metodos-pago")
@RequiredArgsConstructor
public class ClienteMetodoPagoController {
    private final ClienteMetodoPagoService metodoPagoService;

    @GetMapping
    public ResponseEntity<List<ClienteMetodoPago>> listarPorCliente(@RequestParam Long clienteId) {
        return ResponseEntity.ok(metodoPagoService.listarPorCliente(clienteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteMetodoPago> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(metodoPagoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ClienteMetodoPago> crear(@Valid @RequestBody ClienteMetodoPago metodo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(metodoPagoService.crear(metodo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        metodoPagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
