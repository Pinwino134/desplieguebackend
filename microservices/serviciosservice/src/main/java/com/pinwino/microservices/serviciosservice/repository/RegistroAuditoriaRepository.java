package com.pinwino.microservices.serviciosservice.repository;

import com.pinwino.microservices.serviciosservice.model.RegistroAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistroAuditoriaRepository extends JpaRepository<RegistroAuditoria, Long> {

    List<RegistroAuditoria> findTop200ByOrderByFechaEventoDesc();

    List<RegistroAuditoria> findByModuloOrderByFechaEventoDesc(String modulo);

    List<RegistroAuditoria> findByUsuarioIdOrderByFechaEventoDesc(Long usuarioId);
}
