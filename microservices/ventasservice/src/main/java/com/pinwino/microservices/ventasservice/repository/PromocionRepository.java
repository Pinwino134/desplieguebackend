package com.pinwino.microservices.ventasservice.repository;

import com.pinwino.microservices.ventasservice.model.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PromocionRepository extends JpaRepository<Promocion, Long> {
    List<Promocion> findByCampanaId(Long campanaId);
}
