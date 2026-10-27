
package com.pinwino.microservices.authservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

/**
 * OJO piojo:
 * Este servicio por ahora maneja el CRUD de usuario.
 *
 * La seguridad real (hash de password, JWT, roles/permisos,
 * control de acceso en el Gateway, etc.) se implementará
 * en una siguiente etapa.
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // Solo escritura: nunca se devuelve en las respuestas. Se valida en
    // crear() (obligatoria al registrar) y es opcional al actualizar.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false, length = 255)
    private String password;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nombres;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String apellidos;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_actor", nullable = false, length = 20)
    private TipoActor tipoActor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoUsuario estado;

    @Column(
        name = "fecha_registro",
        nullable = false,
        updatable = false
    )
    private LocalDateTime fechaRegistro;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @Builder.Default
    @Column(name = "intentos_fallidos", nullable = false)
    private Integer intentosFallidos = 0;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    @PrePersist
    protected void onCreate() {

        this.fechaRegistro = LocalDateTime.now();

        if (this.estado == null) {
            this.estado = EstadoUsuario.ACTIVO;
        }

        if (this.intentosFallidos == null) {
            this.intentosFallidos = 0;
        }

        this.bloqueadoHasta = null;
        this.ultimoAcceso = null;
    }
}

