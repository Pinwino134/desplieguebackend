package com.pinwino.microservices.productosservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Perfil del artesano. Su id es el id del usuario en authservice
 * (el mismo que se guarda en Producto.artesanoId), por eso no se genera.
 */
@Entity
@Table(name = "artesano")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Artesano {

    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    @Size(max = 100)
    @Column(length = 100)
    private String nombres;

    @Size(max = 100)
    @Column(length = 100)
    private String apellidos;

    @Size(max = 20)
    @Column(length = 20)
    private String dni;

    @Size(max = 100)
    @Column(length = 100)
    private String especialidad;

    @Size(max = 100)
    @Column(length = 100)
    private String region;

    @Size(max = 20)
    @Column(length = 20)
    private String telefono;

    @Size(max = 1000)
    @Column(length = 1000)
    private String descripcion;

    @Size(max = 500)
    @Column(name = "foto_url", length = 500)
    private String fotoUrl;
}
