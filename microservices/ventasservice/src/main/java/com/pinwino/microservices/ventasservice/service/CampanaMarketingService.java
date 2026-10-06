package com.pinwino.microservices.ventasservice.service;

import com.pinwino.microservices.ventasservice.model.CampanaMarketing;
import java.util.List;

public interface CampanaMarketingService {
    List<CampanaMarketing> listarTodas();
    CampanaMarketing buscarPorId(Long id);
    CampanaMarketing crear(CampanaMarketing campana);
    CampanaMarketing actualizar(Long id, CampanaMarketing campana);
    void eliminar(Long id);
}
