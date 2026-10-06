package com.pinwino.microservice.logisticaservice.repository;

import com.pinwino.microservice.logisticaservice.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
}
