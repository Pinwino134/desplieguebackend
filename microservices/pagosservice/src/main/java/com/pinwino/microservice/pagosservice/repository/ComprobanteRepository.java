package com.pinwino.microservice.pagosservice.repository;

import com.pinwino.microservice.pagosservice.model.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {
    Optional<Comprobante> findByPagoId(Long pagoId);
}
