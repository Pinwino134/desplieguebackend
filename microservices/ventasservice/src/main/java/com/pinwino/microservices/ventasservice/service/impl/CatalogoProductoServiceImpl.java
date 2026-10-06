package com.pinwino.microservices.ventasservice.service.impl;

import com.pinwino.microservices.ventasservice.exception.ResourceNotFoundException;
import com.pinwino.microservices.ventasservice.model.CatalogoProducto;
import com.pinwino.microservices.ventasservice.repository.CatalogoProductoRepository;
import com.pinwino.microservices.ventasservice.service.CatalogoProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogoProductoServiceImpl implements CatalogoProductoService {
    private final CatalogoProductoRepository catalogoProductoRepository;

    @Override @Transactional(readOnly = true)
    public List<CatalogoProducto> listarTodos() { return catalogoProductoRepository.findAll(); }

    @Override @Transactional(readOnly = true)
    public List<CatalogoProducto> listarPorCategoria(String categoria) {
        return catalogoProductoRepository.findByCategoria(categoria);
    }

    @Override @Transactional(readOnly = true)
    public CatalogoProducto buscarPorId(Long productoId) {
        return catalogoProductoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("CatalogoProducto", productoId));
    }

    @Override @Transactional
    public CatalogoProducto upsert(CatalogoProducto producto) {
        producto.setActualizadoEn(LocalDateTime.now());
        return catalogoProductoRepository.save(producto);
    }

    @Override @Transactional
    public void eliminar(Long productoId) {
        catalogoProductoRepository.delete(buscarPorId(productoId));
    }
}
