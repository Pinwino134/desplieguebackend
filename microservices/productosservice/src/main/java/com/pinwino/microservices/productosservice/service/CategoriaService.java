package com.pinwino.microservices.productosservice.service;

import com.pinwino.microservices.productosservice.model.Categoria;

import java.util.List;

public interface CategoriaService {

    List<Categoria> listarTodas();

    Categoria buscarPorId(Long id);

    Categoria crear(Categoria categoria);

    Categoria actualizar(Long id, Categoria categoria);

    Categoria activar(Long id);

    Categoria desactivar(Long id);
}