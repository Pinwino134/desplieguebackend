package com.pinwino.microservice.gerenciaservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "presupuesto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Presupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String area;

    @Column(length = 7)
    private String periodo;

    @Column(name = "monto_asignado", precision = 12, scale = 2)
    private BigDecimal montoAsignado;

    @Column(name = "monto_ejecutado", precision = 12, scale = 2)
    private BigDecimal montoEjecutado;
}
