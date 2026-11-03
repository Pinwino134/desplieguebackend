package com.pinwino.microservice.gerenciaservice.config;

import com.pinwino.microservice.gerenciaservice.model.*;
import com.pinwino.microservice.gerenciaservice.repository.CambioHistorialRepository;
import com.pinwino.microservice.gerenciaservice.repository.CambioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Carga, solo si la tabla está vacía, el registro histórico de los cambios que
 * ya se hicieron a la plataforma (reconstruido a partir del trabajo realizado),
 * para que el registro de cambios y sus indicadores no arranquen vacíos.
 * Autor y aprobador 0 = "equipo del proyecto".
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CambiosHistoricosRunner implements CommandLineRunner {

    private static final String NOTA = " [Registro histórico reconstruido a partir del trabajo realizado]";

    private final CambioRepository cambioRepository;
    private final CambioHistorialRepository historialRepository;

    @Override
    public void run(String... args) {
        if (cambioRepository.count() > 0) {
            return;
        }

        cargar("Corregir CORS del API Gateway",
                "El gateway no compilaba por un error de sintaxis en CorsConfig y el origen de Vercel llevaba una barra final. Se reescribió con patrones para localhost y Vercel.",
                TipoCambio.NORMAL, ComponenteCambio.BACKEND, NivelCambio.ALTO, NivelCambio.BAJO,
                "Sin CORS correcto el frontend no podía consumir la API.",
                "Reescribir CorsConfig.java y reconstruir el gateway.",
                "Restaurar la versión anterior del archivo y reconstruir el gateway.",
                LocalDateTime.of(2026, 10, 27, 9, 0));

        cargar("Restringir el registro público de cuentas administrativas",
                "El registro público aceptaba tipoActor ADMINISTRADOR o GERENCIA. Ahora, sin sesión de administrador, solo se crean clientes y artesanos.",
                TipoCambio.EMERGENCIA, ComponenteCambio.BACKEND, NivelCambio.ALTO, NivelCambio.MEDIO,
                "Vulnerabilidad: cualquier persona podía crearse una cuenta administradora.",
                "Validar el rol en UsuarioController y reconstruir authservice.",
                "Revertir el controlador a la versión anterior (reabre la vulnerabilidad).",
                LocalDateTime.of(2026, 10, 27, 10, 0));

        cargar("Ocultar la contraseña en las respuestas de usuarios",
                "GET /usuarios devolvía el hash de la contraseña. El campo pasó a ser solo de escritura y se ajustó la validación al actualizar.",
                TipoCambio.EMERGENCIA, ComponenteCambio.BACKEND, NivelCambio.ALTO, NivelCambio.MEDIO,
                "Exposición de datos sensibles hacia el frontend.",
                "Marcar password como WRITE_ONLY, exigirla solo al crear y reconstruir authservice.",
                "Quitar la anotación WRITE_ONLY (vuelve a exponer el hash).",
                LocalDateTime.of(2026, 10, 27, 11, 0));

        cargar("Crear el perfil de artesano en el backend",
                "La pantalla de perfil de artesano llamaba a /artesanos/{id}, que no existía. Se creó la entidad, el repositorio y el controlador con control de propiedad.",
                TipoCambio.NORMAL, ComponenteCambio.BACKEND, NivelCambio.MEDIO, NivelCambio.BAJO,
                "Funcionalidad rota: el artesano no podía ver ni editar su perfil.",
                "Agregar Artesano, ArtesanoRepository y ArtesanoController en productosservice y la ruta en el gateway.",
                "Quitar los tres archivos y la ruta; la tabla artesano queda sin uso.",
                LocalDateTime.of(2026, 10, 27, 14, 0));

        cargar("Unificar el stock y descontarlo al confirmar el pago",
                "El catálogo usaba el stock del producto y el pago consultaba el inventario de logística (vacío). Ahora hay una sola fuente y el stock se descuenta al pagar mediante un endpoint interno bloqueado en el gateway.",
                TipoCambio.NORMAL, ComponenteCambio.BACKEND, NivelCambio.ALTO, NivelCambio.MEDIO,
                "Los clientes no podían comprar productos con stock disponible.",
                "Validar contra ProductoDTO.stock, agregar el descuento en productosservice y reconstruir ventas, productos y gateway.",
                "Restaurar PedidoServiceImpl y quitar el endpoint de descuento.",
                LocalDateTime.of(2026, 10, 28, 9, 0));

        cargar("Crear el almacén por defecto al arrancar logística",
                "ventasservice despacha desde el almacén 1, que no existía, y todos los pagos fallaban. Se agregó un arranque que lo crea con id 1 si falta.",
                TipoCambio.EMERGENCIA, ComponenteCambio.BASE_DE_DATOS, NivelCambio.ALTO, NivelCambio.BAJO,
                "Error 404 en el envío: ningún pago se podía completar.",
                "Agregar AlmacenInicialRunner en logisticaservice y reconstruirlo.",
                "Borrar la fila del almacén y quitar el runner.",
                LocalDateTime.of(2026, 10, 28, 12, 0));

        cargar("Mover el descuento de stock al final del pago",
                "El descuento ocurría antes de crear el envío: un fallo dejaba el stock reducido y el pedido revertido. Ahora es la última operación del flujo.",
                TipoCambio.EMERGENCIA, ComponenteCambio.BACKEND, NivelCambio.MEDIO, NivelCambio.BAJO,
                "Pérdida de stock en cada intento de pago fallido.",
                "Reordenar las llamadas en PedidoServiceImpl.confirmarPago y reconstruir ventasservice.",
                "Restaurar el orden anterior del método.",
                LocalDateTime.of(2026, 10, 28, 13, 0));

        cargar("Incorporar monitoreo con Prometheus y Grafana",
                "Se agregó Actuator y Micrometer a los 9 servicios, Prometheus para recolectar métricas, Grafana con un dashboard aprovisionado y JMeter para pruebas de carga.",
                TipoCambio.NORMAL, ComponenteCambio.INFRAESTRUCTURA, NivelCambio.MEDIO, NivelCambio.MEDIO,
                "Medir el rendimiento y la capacidad del servicio.",
                "Agregar dependencias y configuración, ampliar docker-compose y reconstruir todo el backend.",
                "Quitar los servicios de monitoreo del compose y las dependencias de los pom.xml.",
                LocalDateTime.of(2026, 10, 30, 9, 0));

        cargar("Incorporar la gestión de solicitudes de servicio",
                "Nuevo módulo para que clientes y artesanos registren solicitudes con prioridad, SLA e historial, y para que administración las atienda.",
                TipoCambio.NORMAL, ComponenteCambio.BACKEND, NivelCambio.MEDIO, NivelCambio.BAJO,
                "Requisito del curso: gestión de solicitudes (ITIL).",
                "Agregar modelo, servicio y controlador en ventasservice, rutas en el gateway y las pantallas del frontend.",
                "Quitar la ruta del gateway y los archivos del módulo; las tablas quedan sin uso.",
                LocalDateTime.of(2026, 11, 1, 9, 0));

        log.info("[CambiosHistoricosRunner] Registro histórico de cambios cargado");
    }

    private void cargar(String titulo, String descripcion, TipoCambio tipo, ComponenteCambio componente,
                        NivelCambio impacto, NivelCambio riesgo, String motivo,
                        String planImpl, String planReversa, LocalDateTime fecha) {

        boolean emergencia = tipo == TipoCambio.EMERGENCIA;
        LocalDateTime decision = fecha.plusHours(emergencia ? 1 : 3);
        LocalDateTime implementacion = decision.plusHours(2);
        LocalDateTime cierre = implementacion.plusDays(1);

        Cambio c = cambioRepository.save(Cambio.builder()
                .titulo(titulo)
                .descripcion(descripcion + NOTA)
                .tipo(tipo)
                .componente(componente)
                .impacto(impacto)
                .riesgo(riesgo)
                .motivo(motivo)
                .planImplementacion(planImpl)
                .planReversa(planReversa)
                .fechaPlanificada(implementacion)
                .solicitanteId(0L)
                .solicitanteTipo("ADMINISTRADOR")
                .estado(EstadoCambio.CERRADO)
                .aprobadorId(0L)
                .fechaDecision(decision)
                .motivoDecision("Aprobado por el equipo del proyecto")
                .implementadorId(0L)
                .fechaImplementacion(implementacion)
                .resultado(ResultadoCambio.EXITOSO)
                .notasImplementacion("Implementado y verificado en el entorno de pruebas")
                .fechaCierre(cierre)
                .fechaCreacion(fecha)
                .fechaActualizacion(cierre)
                .build());

        evento(c.getId(), "CREADO", "Cambio propuesto (" + tipo.getNombre() + ")", fecha);
        evento(c.getId(), "DECISION", "APROBADO", decision);
        evento(c.getId(), "IMPLEMENTACION", "Implementado con resultado EXITOSO", implementacion);
        evento(c.getId(), "CAMBIO_ESTADO", "IMPLEMENTADO → CERRADO", cierre);
    }

    private void evento(Long cambioId, String evento, String detalle, LocalDateTime fecha) {
        historialRepository.save(CambioHistorial.builder()
                .cambioId(cambioId)
                .autorId(0L)
                .autorTipo("ADMINISTRADOR")
                .evento(evento)
                .detalle(detalle)
                .fecha(fecha)
                .build());
    }
}
