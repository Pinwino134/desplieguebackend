package com.pinwino.microservices.ventasservice.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "direccion_envio_id")
    private Long direccionEnvioId;

    @Column(name = "fecha_pedido", nullable = false, updatable = false)
    private LocalDateTime fechaPedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPedido estado;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    /** CATALOGO o PERSONALIZADO */
    @Builder.Default
    @Column(length = 20)
    private String tipo = "CATALOGO";

    /** Artesano destinatario (usado en pedidos PERSONALIZADO). */
    @Column(name = "artesano_id")
    private Long artesanoId;

    @Column(name = "descripcion_personalizada", length = 1500)
    private String descripcionPersonalizada;

    @Column(name = "presupuesto_referencial", precision = 10, scale = 2)
    private BigDecimal presupuestoReferencial;

    @Column(name = "precio_cotizado", precision = 10, scale = 2)
    private BigDecimal precioCotizado;

    @Column(name = "dias_estimados")
    private Integer diasEstimados;

    @Column(name = "nota_artesano", length = 1000)
    private String notaArtesano;

    /** Porcentaje de avance de producción (0-100) actualizado por el artesano. */
    @Builder.Default
    @Column(name = "avance_produccion")
    private Integer avanceProduccion = 0;

    /** Solo si pagado == true el artesano puede iniciar la producción. */
    @Builder.Default
    @Column(name = "pagado")
    private Boolean pagado = false;

    @Column(name = "numero_boleta", length = 40)
    private String numeroBoleta;

    @Column(name = "empresa_envio", length = 80)
    private String empresaEnvio;

    @Column(name = "codigo_seguimiento", length = 80)
    private String codigoSeguimiento;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    /** Datos de cancelación y reembolso bancario simulado */
    @Column(name = "motivo_cancelacion", length = 500)
    private String motivoCancelacion;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    @Column(name = "banco_reembolso", length = 60)
    private String bancoReembolso;

    @Column(name = "cuenta_reembolso", length = 60)
    private String cuentaReembolso;

    @Column(name = "referencia_reembolso", length = 60)
    private String referenciaReembolso;

    @Column(name = "monto_reembolsado", precision = 10, scale = 2)
    private BigDecimal montoReembolsado;

    @Builder.Default
    @JsonManagedReference
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.fechaPedido = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoPedido.PENDIENTE_PAGO;
        }
        if (this.tipo == null || this.tipo.isBlank()) {
            this.tipo = "CATALOGO";
        }
        if (this.avanceProduccion == null) {
            this.avanceProduccion = 0;
        }
        if (this.pagado == null) {
            this.pagado = false;
        }
    }
}
