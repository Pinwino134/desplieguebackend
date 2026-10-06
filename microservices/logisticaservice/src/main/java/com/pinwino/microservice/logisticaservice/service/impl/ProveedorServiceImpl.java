package com.pinwino.microservice.logisticaservice.service.impl;

import com.pinwino.microservice.logisticaservice.exception.ResourceNotFoundException;
import com.pinwino.microservice.logisticaservice.model.Proveedor;
import com.pinwino.microservice.logisticaservice.repository.ProveedorRepository;
import com.pinwino.microservice.logisticaservice.service.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProveedorServiceImpl implements ProveedorService {
    private final ProveedorRepository proveedorRepository;

    @Override @Transactional(readOnly = true)
    public List<Proveedor> listarTodos() { return proveedorRepository.findAll(); }

    @Override @Transactional(readOnly = true)
    public Proveedor buscarPorId(Long id) {
        return proveedorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Proveedor", id));
    }

    @Override @Transactional
    public Proveedor crear(Proveedor proveedor) { return proveedorRepository.save(proveedor); }

    @Override @Transactional
    public Proveedor actualizar(Long id, Proveedor datos) {
        Proveedor existente = buscarPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setContacto(datos.getContacto());
        existente.setTipoInsumo(datos.getTipoInsumo());
        return proveedorRepository.save(existente);
    }

    @Override @Transactional
    public void eliminar(Long id) { proveedorRepository.delete(buscarPorId(id)); }
}
