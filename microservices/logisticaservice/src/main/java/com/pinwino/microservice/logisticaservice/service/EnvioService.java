package com.pinwino.microservice.logisticaservice.service;

import com.pinwino.microservice.logisticaservice.model.EstadoEnvio;
import com.pinwino.microservice.logisticaservice.model.Envio;
import com.pinwino.microservice.logisticaservice.model.SeguimientoEnvio;
import java.util.List;

public interface EnvioService {
    List<Envio> listarTodos();
    List<Envio> listarPorEstado(EstadoEnvio estado);
    Envio buscarPorId(Long id);
    Envio buscarPorPedido(Long pedidoId);
    // Normalmente disparado por el evento PedidoConfirmado desde ventas_db
    Envio crear(Long almacenId, Long transportistaId, Envio envio);
    SeguimientoEnvio agregarSeguimiento(Long envioId, SeguimientoEnvio seguimiento);
    Envio cambiarEstado(Long id, EstadoEnvio estado);
}
