package com.pinwino.microservices.ventasservice.service.impl;

import com.pinwino.microservices.ventasservice.exception.ResourceNotFoundException;
import com.pinwino.microservices.ventasservice.model.Resena;
import com.pinwino.microservices.ventasservice.repository.ResenaRepository;
import com.pinwino.microservices.ventasservice.service.ResenaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResenaServiceImpl implements ResenaService {
    private final ResenaRepository resenaRepository;

    @Override @Transactional(readOnly = true)
    public List<Resena> listarPorProducto(Long productoId) { return resenaRepository.findByProductoId(productoId); }

    @Override @Transactional
    public Resena crear(Resena resena) { return resenaRepository.save(resena); }

    @Override @Transactional
    public void eliminar(Long id) {
        Resena existente = resenaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resena", id));
        resenaRepository.delete(existente);
    }
}
