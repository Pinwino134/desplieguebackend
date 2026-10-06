package com.pinwino.microservices.productosservice.service;

import com.pinwino.microservices.productosservice.model.EstadoProducto;
import com.pinwino.microservices.productosservice.model.Producto;
import com.pinwino.microservices.productosservice.model.ProductoMaterial;
import com.pinwino.microservices.productosservice.model.ProcesoProduccion;

import java.util.List;

public interface ProductoService {

    List<Producto> listarTodos();

    List<Producto> listarPorArtesano(Long artesanoId);

    List<Producto> listarPorCategoria(Long categoriaId);

    List<Producto> listarPorEstado(EstadoProducto estado);

    Producto buscarPorId(Long id);

    Producto crear(
            Long artesanoId,
            Long categoriaId,
            Producto producto
    );

    Producto actualizar(
            Long id,
            Producto producto
    );

    Producto activar(Long id);

    Producto desactivar(Long id);

    // Gestión de materiales del producto
    ProductoMaterial agregarMaterial(
            Long productoId,
            Long materialId,
            ProductoMaterial detalle
    );

    void quitarMaterial(
            Long productoId,
            Long productoMaterialId
    );

    // Gestión del proceso de producción
    ProcesoProduccion agregarProceso(
            Long productoId,
            ProcesoProduccion proceso
    );

    void quitarProceso(
            Long productoId,
            Long procesoId
    );
}