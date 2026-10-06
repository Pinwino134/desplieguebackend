package com.pinwino.microservices.productosservice.repository;

import com.pinwino.microservices.productosservice.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, Long> {
}
