package com.pinwino.microservices.productosservice.repository;

import com.pinwino.microservices.productosservice.model.ProcesoProduccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcesoProduccionRepository extends JpaRepository<ProcesoProduccion, Long> {
	List<ProcesoProduccion> findByProductoIdOrderByOrdenEtapaAsc(Long productoId);
}
