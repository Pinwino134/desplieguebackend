package com.pinwino.microservice.gerenciaservice.repository;

import com.pinwino.microservice.gerenciaservice.model.ReporteArtesanos;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReporteArtesanosRepository extends JpaRepository<ReporteArtesanos, Long> {
    List<ReporteArtesanos> findByPeriodo(String periodo);
    List<ReporteArtesanos> findByArtesanoId(Long artesanoId);
}
