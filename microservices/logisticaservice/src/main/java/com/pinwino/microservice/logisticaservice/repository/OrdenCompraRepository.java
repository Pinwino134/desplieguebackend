package com.pinwino.microservice.logisticaservice.repository;

import com.pinwino.microservice.logisticaservice.model.EstadoOrdenCompra;
import com.pinwino.microservice.logisticaservice.model.OrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {
    List<OrdenCompra> findByProveedorId(Long proveedorId);
    List<OrdenCompra> findByEstado(EstadoOrdenCompra estado);
}
