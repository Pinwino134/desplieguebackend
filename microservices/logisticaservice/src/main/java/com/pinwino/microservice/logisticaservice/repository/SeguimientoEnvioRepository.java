package com.pinwino.microservice.logisticaservice.repository;

import com.pinwino.microservice.logisticaservice.model.SeguimientoEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SeguimientoEnvioRepository extends JpaRepository<SeguimientoEnvio, Long> {
    List<SeguimientoEnvio> findByEnvioIdOrderByFechaHoraAsc(Long envioId);
}
