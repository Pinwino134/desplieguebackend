package com.pinwino.microservice.logisticaservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Garantiza que exista el almacén con id 1.
 *
 * ventasservice despacha cada pedido pagado desde el almacén con id
 * "logistica.almacen-default-id" (1 en configserver). Sin él, el pago falla
 * con "Almacen con id 1 no encontrado".
 *
 * Se inserta con id explícito (JPA no deja forzar el id en columnas
 * IDENTITY) y después se sincroniza la secuencia para que los próximos
 * almacenes no choquen con él. Es idempotente: si el id 1 ya existe, no hace nada.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlmacenInicialRunner implements CommandLineRunner {

    private final JdbcTemplate jdbc;

    @Override
    public void run(String... args) {
        int creados = jdbc.update("""
                INSERT INTO almacen (id, nombre, ubicacion, capacidad)
                SELECT 1, 'Almacén Central', 'Arequipa, Perú', 1000
                WHERE NOT EXISTS (SELECT 1 FROM almacen WHERE id = 1)
                """);

        if (creados > 0) {
            jdbc.queryForObject("""
                    SELECT setval(pg_get_serial_sequence('almacen', 'id'),
                                  (SELECT MAX(id) FROM almacen))
                    """, Long.class);
            log.info("[AlmacenInicialRunner] Almacén por defecto creado con id 1");
        }
    }
}
