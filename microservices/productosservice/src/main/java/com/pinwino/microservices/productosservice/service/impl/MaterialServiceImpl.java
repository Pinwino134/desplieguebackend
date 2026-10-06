package com.pinwino.microservices.productosservice.service.impl;

import com.pinwino.microservices.productosservice.exception.ResourceNotFoundException;
import com.pinwino.microservices.productosservice.model.EstadoMaterial;
import com.pinwino.microservices.productosservice.model.Material;
import com.pinwino.microservices.productosservice.repository.MaterialRepository;
import com.pinwino.microservices.productosservice.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Material> listarTodos() {
        return materialRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Material buscarPorId(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Material", id));
    }

    @Override
    @Transactional
    public Material crear(Material material) {

        material.setEstado(EstadoMaterial.ACTIVO);

        return materialRepository.save(material);
    }

    @Override
    @Transactional
    public Material actualizar(Long id, Material datos) {

        Material existente = buscarPorId(id);

        existente.setNombre(datos.getNombre());
        existente.setTipo(datos.getTipo());
        existente.setOrigen(datos.getOrigen());

        return materialRepository.save(existente);
    }

    @Override
    @Transactional
    public Material activar(Long id) {

        Material material = buscarPorId(id);

        material.setEstado(EstadoMaterial.ACTIVO);

        return materialRepository.save(material);
    }

    @Override
    @Transactional
    public Material desactivar(Long id) {

        Material material = buscarPorId(id);

        material.setEstado(EstadoMaterial.INACTIVO);

        return materialRepository.save(material);
    }
}