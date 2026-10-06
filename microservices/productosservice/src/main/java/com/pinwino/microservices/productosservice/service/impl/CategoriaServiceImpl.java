package com.pinwino.microservices.productosservice.service.impl;

import com.pinwino.microservices.productosservice.exception.BusinessException;
import com.pinwino.microservices.productosservice.exception.ResourceNotFoundException;
import com.pinwino.microservices.productosservice.model.Categoria;
import com.pinwino.microservices.productosservice.model.EstadoCategoria;
import com.pinwino.microservices.productosservice.repository.CategoriaRepository;
import com.pinwino.microservices.productosservice.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Categoria", id));
    }

    @Override
    @Transactional
    public Categoria crear(Categoria categoria) {

        if (categoriaRepository.existsByNombre(categoria.getNombre())) {
            throw new BusinessException(
                    "Ya existe una categoria con nombre "
                            + categoria.getNombre()
            );
        }

        categoria.setEstado(EstadoCategoria.ACTIVA);

        return categoriaRepository.save(categoria);
    }

    @Override
    @Transactional
    public Categoria actualizar(Long id, Categoria datos) {

        Categoria existente = buscarPorId(id);

        existente.setNombre(datos.getNombre());
        existente.setDescripcion(datos.getDescripcion());

        return categoriaRepository.save(existente);
    }

    @Override
    @Transactional
    public Categoria activar(Long id) {

        Categoria categoria = buscarPorId(id);

        categoria.setEstado(EstadoCategoria.ACTIVA);

        return categoriaRepository.save(categoria);
    }

    @Override
    @Transactional
    public Categoria desactivar(Long id) {

        Categoria categoria = buscarPorId(id);

        categoria.setEstado(EstadoCategoria.INACTIVA);

        return categoriaRepository.save(categoria);
    }
}