package com.pinwino.microservices.productosservice.service;

import com.pinwino.microservices.productosservice.model.Material;

import java.util.List;

public interface MaterialService {

    List<Material> listarTodos();

    Material buscarPorId(Long id);

    Material crear(Material material);

    Material actualizar(Long id, Material material);

    Material activar(Long id);

    Material desactivar(Long id);
}