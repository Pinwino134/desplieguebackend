package com.pinwino.microservices.ventasservice.repository;

import com.pinwino.microservices.ventasservice.model.CarritoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long> {
    List<CarritoItem> findByCarritoId(Long carritoId);
}
