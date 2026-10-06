package com.pinwino.microservices.ventasservice.service.impl;

import com.pinwino.microservices.ventasservice.exception.ResourceNotFoundException;
import com.pinwino.microservices.ventasservice.model.CampanaMarketing;
import com.pinwino.microservices.ventasservice.model.ProductoPromocion;
import com.pinwino.microservices.ventasservice.model.Promocion;
import com.pinwino.microservices.ventasservice.repository.CampanaMarketingRepository;
import com.pinwino.microservices.ventasservice.repository.ProductoPromocionRepository;
import com.pinwino.microservices.ventasservice.repository.PromocionRepository;
import com.pinwino.microservices.ventasservice.service.PromocionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromocionServiceImpl implements PromocionService {
    private final PromocionRepository promocionRepository;
    private final CampanaMarketingRepository campanaRepository;
    private final ProductoPromocionRepository productoPromocionRepository;

    @Override @Transactional(readOnly = true)
    public List<Promocion> listarPorCampana(Long campanaId) { return promocionRepository.findByCampanaId(campanaId); }

    @Override @Transactional(readOnly = true)
    public Promocion buscarPorId(Long id) {
        return promocionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Promocion", id));
    }

    @Override @Transactional
    public Promocion crear(Long campanaId, Promocion promocion) {
        CampanaMarketing campana = campanaRepository.findById(campanaId)
                .orElseThrow(() -> new ResourceNotFoundException("CampanaMarketing", campanaId));
        promocion.setCampana(campana);
        return promocionRepository.save(promocion);
    }

    @Override @Transactional
    public void eliminar(Long id) { promocionRepository.delete(buscarPorId(id)); }

    @Override @Transactional
    public ProductoPromocion aplicarAProducto(Long promocionId, Long productoId) {
        Promocion promocion = buscarPorId(promocionId);
        ProductoPromocion pp = ProductoPromocion.builder().promocion(promocion).productoId(productoId).build();
        return productoPromocionRepository.save(pp);
    }

    @Override @Transactional
    public void quitarDeProducto(Long productoPromocionId) {
        ProductoPromocion pp = productoPromocionRepository.findById(productoPromocionId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductoPromocion", productoPromocionId));
        productoPromocionRepository.delete(pp);
    }
}
