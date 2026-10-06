package com.pinwino.microservice.pagosservice.repository;

import com.pinwino.microservice.pagosservice.model.Reembolso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReembolsoRepository extends JpaRepository<Reembolso, Long> {
    List<Reembolso> findByPagoId(Long pagoId);
}
