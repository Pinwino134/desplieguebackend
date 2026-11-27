package com.pinwino.microservices.serviciosservice.service;

import com.pinwino.microservices.serviciosservice.model.RegistroAuditoria;

import java.util.List;

public interface AuditoriaService {

    RegistroAuditoria registrar(RegistroAuditoria registro);

    List<RegistroAuditoria> listar(String modulo, Long usuarioId);
}
