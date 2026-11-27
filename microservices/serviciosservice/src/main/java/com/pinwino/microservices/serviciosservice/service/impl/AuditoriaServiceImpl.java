package com.pinwino.microservices.serviciosservice.service.impl;

import com.pinwino.microservices.serviciosservice.model.RegistroAuditoria;
import com.pinwino.microservices.serviciosservice.repository.RegistroAuditoriaRepository;
import com.pinwino.microservices.serviciosservice.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaServiceImpl implements AuditoriaService {

    private final RegistroAuditoriaRepository auditoriaRepository;

    @Override
    @Transactional
    public RegistroAuditoria registrar(RegistroAuditoria registro) {
        if (registro.getResultado() == null || registro.getResultado().isBlank()) {
            registro.setResultado("EXITO");
        }
        return auditoriaRepository.save(registro);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistroAuditoria> listar(String modulo, Long usuarioId) {
        if (modulo != null && !modulo.isBlank()) {
            return auditoriaRepository.findByModuloOrderByFechaEventoDesc(modulo);
        }
        if (usuarioId != null) {
            return auditoriaRepository.findByUsuarioIdOrderByFechaEventoDesc(usuarioId);
        }
        return auditoriaRepository.findTop200ByOrderByFechaEventoDesc();
    }
}
