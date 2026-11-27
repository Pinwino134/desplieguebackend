package com.pinwino.microservices.productosservice.repository;

import com.pinwino.microservices.productosservice.model.EstadoRevisionLogro;
import com.pinwino.microservices.productosservice.model.LogroArtesano;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogroArtesanoRepository extends JpaRepository<LogroArtesano, Long> {

    List<LogroArtesano> findByArtesanoIdOrderByFechaCreacionDesc(Long artesanoId);

    List<LogroArtesano> findByArtesanoIdAndEstadoOrderByAnioDesc(Long artesanoId, EstadoRevisionLogro estado);

    List<LogroArtesano> findByEstadoOrderByFechaCreacionDesc(EstadoRevisionLogro estado);

    List<LogroArtesano> findAllByOrderByFechaCreacionDesc();
}
