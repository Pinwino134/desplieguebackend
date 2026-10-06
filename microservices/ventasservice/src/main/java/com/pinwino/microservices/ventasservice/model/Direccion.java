package com.pinwino.microservices.ventasservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "direccion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonBackReference
    private Cliente cliente;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String calle;

    @Column(length = 80)
    private String distrito;

    @Column(length = 80)
    private String ciudad;

    @Column(length = 255)
    private String referencia;

    @Column(name = "es_principal")
    private Boolean esPrincipal;
}
