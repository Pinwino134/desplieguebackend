package com.pinwino.microservices.ventasservice.controller;

import com.pinwino.microservices.ventasservice.model.Cliente;
import com.pinwino.microservices.ventasservice.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {
    private final ClienteService clienteService;

    @GetMapping
    public ResponseEntity<List<Cliente>> listar() { return ResponseEntity.ok(clienteService.listarTodos()); }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarPorId(@PathVariable Long id) { return ResponseEntity.ok(clienteService.buscarPorId(id)); }

    @GetMapping("/por-usuario/{usuarioId}")
    public ResponseEntity<Cliente> buscarPorUsuarioId(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(clienteService.buscarPorUsuarioId(usuarioId));
    }

    @PostMapping
    public ResponseEntity<Cliente> crear(@Valid @RequestBody Cliente cliente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(cliente));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizar(@PathVariable Long id, @Valid @RequestBody Cliente cliente) {
        return ResponseEntity.ok(clienteService.actualizar(id, cliente));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
