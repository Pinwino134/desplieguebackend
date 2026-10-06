package com.pinwino.microservices.productosservice.service.impl;

import com.pinwino.microservices.productosservice.exception.BusinessException;
import com.pinwino.microservices.productosservice.exception.ResourceNotFoundException;
import com.pinwino.microservices.productosservice.model.*;
import com.pinwino.microservices.productosservice.repository.*;
import com.pinwino.microservices.productosservice.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MaterialRepository materialRepository;
    private final ProductoMaterialRepository productoMaterialRepository;
    private final ProcesoProduccionRepository procesoProduccionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarPorArtesano(Long artesanoId) {
        return productoRepository.findByArtesanoId(artesanoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarPorCategoria(Long categoriaId) {
        return productoRepository.findByCategoriaId(categoriaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarPorEstado(EstadoProducto estado) {
        return productoRepository.findByEstado(estado);
    }

    @Override
    @Transactional(readOnly = true)
    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Producto", id));
    }

    @Override
    @Transactional
    public Producto crear(
            Long artesanoId,
            Long categoriaId,
            Producto producto) {

        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Categoria",
                                categoriaId
                        ));

        // No se pueden crear productos en categorías inactivas.
        if (categoria.getEstado() == EstadoCategoria.INACTIVA) {
            throw new BusinessException(
                    "No se puede crear un producto en una categoria inactiva"
            );
        }

        // El artesano pertenece al authservice.
        // Solo guardamos su ID en productoservice.
        producto.setArtesanoId(artesanoId);

        producto.setCategoria(categoria);

        // Todo producto nuevo inicia activo.
        if (producto.getEstado() == null) {
            producto.setEstado(EstadoProducto.ACTIVO);
        }

        // Si comienza con stock 0, queda agotado.
        if (producto.getStock() != null
                && producto.getStock() == 0) {

            producto.setEstado(EstadoProducto.AGOTADO);
        }

        return productoRepository.save(producto);
    }

    @Override
    @Transactional
    public Producto actualizar(
            Long id,
            Producto datos) {

        Producto existente = buscarPorId(id);

        existente.setNombre(datos.getNombre());
        existente.setDescripcion(datos.getDescripcion());
        existente.setPrecioBase(datos.getPrecioBase());
        existente.setImagenUrl(datos.getImagenUrl());

        if (datos.getStock() != null) {
            existente.setStock(datos.getStock());

            // Si el stock llega a cero, queda agotado.
            if (datos.getStock() == 0
                    && existente.getEstado() != EstadoProducto.INACTIVO) {

                existente.setEstado(EstadoProducto.AGOTADO);

            // Si vuelve a tener stock y estaba agotado,
            // vuelve a estar activo.
            } else if (datos.getStock() > 0
                    && existente.getEstado() == EstadoProducto.AGOTADO) {

                existente.setEstado(EstadoProducto.ACTIVO);
            }
        }

        return productoRepository.save(existente);
    }

    @Override
    @Transactional
    public Producto activar(Long id) {

        Producto producto = buscarPorId(id);

        if (producto.getStock() != null
                && producto.getStock() == 0) {

            producto.setEstado(EstadoProducto.AGOTADO);

        } else {

            producto.setEstado(EstadoProducto.ACTIVO);
        }

        return productoRepository.save(producto);
    }

    @Override
    @Transactional
    public Producto desactivar(Long id) {

        Producto producto = buscarPorId(id);

        producto.setEstado(EstadoProducto.INACTIVO);

        return productoRepository.save(producto);
    }

    @Override
    @Transactional
    public ProductoMaterial agregarMaterial(
            Long productoId,
            Long materialId,
            ProductoMaterial detalle) {

        Producto producto = buscarPorId(productoId);

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Material",
                                materialId
                        ));

        if (material.getEstado() == EstadoMaterial.INACTIVO) {
            throw new BusinessException(
                    "No se puede agregar un material inactivo"
            );
        }

        detalle.setProducto(producto);
        detalle.setMaterial(material);

        return productoMaterialRepository.save(detalle);
    }

    @Override
    @Transactional
    public void quitarMaterial(
            Long productoId,
            Long productoMaterialId) {

        ProductoMaterial pm =
                productoMaterialRepository.findById(productoMaterialId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "ProductoMaterial",
                                        productoMaterialId
                                ));

        // Verificamos que la relación pertenezca al producto.
        if (!pm.getProducto().getId().equals(productoId)) {
            throw new BusinessException(
                    "El material no pertenece al producto indicado"
            );
        }

        productoMaterialRepository.delete(pm);
    }

    @Override
    @Transactional
    public ProcesoProduccion agregarProceso(
            Long productoId,
            ProcesoProduccion proceso) {

        Producto producto = buscarPorId(productoId);

        proceso.setProducto(producto);

        return procesoProduccionRepository.save(proceso);
    }

    @Override
    @Transactional
    public void quitarProceso(
            Long productoId,
            Long procesoId) {

        ProcesoProduccion proceso =
                procesoProduccionRepository.findById(procesoId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "ProcesoProduccion",
                                        procesoId
                                ));

        // Verificamos que el proceso pertenezca al producto.
        if (!proceso.getProducto().getId().equals(productoId)) {
            throw new BusinessException(
                    "El proceso no pertenece al producto indicado"
            );
        }

        procesoProduccionRepository.delete(proceso);
    }
}