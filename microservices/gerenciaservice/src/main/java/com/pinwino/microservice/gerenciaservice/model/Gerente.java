package com.pinwino.microservice.gerenciaservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "gerente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gerente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId; // ref externa a auth_db

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(length = 100)
    private String apellidos;

    @Column(length = 80)
    private String cargo;
}
