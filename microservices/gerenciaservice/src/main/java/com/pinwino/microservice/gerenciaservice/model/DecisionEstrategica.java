package com.pinwino.microservice.gerenciaservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "decision_estrategica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionEstrategica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gerente_id", nullable = false)
    @JsonBackReference
    private Gerente gerente;

    @NotBlank
    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(name = "area_impacto", length = 80)
    private String areaImpacto;

    @Column(nullable = false, updatable = false)
    private LocalDate fecha;

    @PrePersist
    protected void onCreate() {
        this.fecha = LocalDate.now();
    }
}
