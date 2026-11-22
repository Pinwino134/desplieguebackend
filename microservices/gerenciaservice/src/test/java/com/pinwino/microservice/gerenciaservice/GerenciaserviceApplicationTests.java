package com.pinwino.microservice.gerenciaservice;

import com.pinwino.microservice.gerenciaservice.model.KPI;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class GerenciaserviceApplicationTests {

    @Test
    void kpiEstrategicoCalculaCumplimientoDeMeta() {
        KPI kpi = KPI.builder()
                .nombre("Ventas Mensuales Arte Andino")
                .valor(new BigDecimal("42500.00"))
                .meta(new BigDecimal("40000.00"))
                .periodo("2026-06")
                .build();

        assertTrue(kpi.getValor().compareTo(kpi.getMeta()) >= 0);
        assertEquals("2026-06", kpi.getPeriodo());
    }
}
