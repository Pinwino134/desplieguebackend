package com.pinwino.microservice.logisticaservice;

import com.pinwino.microservice.logisticaservice.model.EstadoEnvio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogisticaserviceApplicationTests {

    @Test
    void cicloDeEstadosDeEnvioEstaCompleto() {
        assertEquals(4, EstadoEnvio.values().length);
        assertNotNull(EstadoEnvio.valueOf("PREPARANDO"));
        assertNotNull(EstadoEnvio.valueOf("EN_RUTA"));
        assertNotNull(EstadoEnvio.valueOf("ENTREGADO"));
        assertNotNull(EstadoEnvio.valueOf("DEVUELTO"));
    }
}
