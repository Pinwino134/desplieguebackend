package com.pinwino.microservices.productosservice.repository;

import com.pinwino.microservices.productosservice.model.Artesano;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtesanoRepository extends JpaRepository<Artesano, Long> {
}
