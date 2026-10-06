package com.pinwino.microservices.ventasservice.service.impl;

import com.pinwino.microservices.ventasservice.client.ProductoClient;
import com.pinwino.microservices.ventasservice.exception.BusinessException;
import com.pinwino.microservices.ventasservice.exception.ResourceNotFoundException;
import com.pinwino.microservices.ventasservice.model.Carrito;
import com.pinwino.microservices.ventasservice.model.CarritoItem;
import com.pinwino.microservices.ventasservice.repository.CarritoItemRepository;
import com.pinwino.microservices.ventasservice.repository.CarritoRepository;
import com.pinwino.microservices.ventasservice.service.CarritoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CarritoServiceImpl implements CarritoService {
    private final CarritoRepository carritoRepository;
    private final CarritoItemRepository carritoItemRepository;
    private final ProductoClient productoClient;

    @Override @Transactional
    public Carrito obtenerOCrearPorCliente(Long clienteId) {
        return carritoRepository.findByClienteId(clienteId)
                .orElseGet(() -> carritoRepository.save(Carrito.builder().clienteId(clienteId).build()));
    }

    @Override @Transactional(readOnly = true)
    public Carrito buscarPorId(Long id) {
        return carritoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Carrito", id));
    }

    @Override @Transactional
    public CarritoItem agregarItem(Long carritoId, CarritoItem item) {
        Carrito carrito = buscarPorId(carritoId);

        var producto = productoClient.buscarPorId(item.getProductoId());
        if (producto == null) {
            throw new ResourceNotFoundException("Producto", item.getProductoId());
        }
        if (!"ACTIVO".equals(producto.getEstado())) {
            throw new BusinessException("El producto '" + producto.getNombre() + "' no está disponible.");
        }

        item.setCarrito(carrito);
        item.setPrecioUnitario(producto.getPrecioBase());
        return carritoItemRepository.save(item);
    }

    @Override @Transactional
    public CarritoItem actualizarItem(Long itemId, CarritoItem datos) {
        CarritoItem existente = carritoItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CarritoItem", itemId));
        existente.setCantidad(datos.getCantidad());
        return carritoItemRepository.save(existente);
    }

    @Override @Transactional
    public void quitarItem(Long itemId) {
        CarritoItem existente = carritoItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CarritoItem", itemId));
        carritoItemRepository.delete(existente);
    }

    @Override @Transactional
    public void vaciar(Long carritoId) {
        Carrito carrito = buscarPorId(carritoId);
        carritoItemRepository.deleteAll(carrito.getItems());
    }
}
