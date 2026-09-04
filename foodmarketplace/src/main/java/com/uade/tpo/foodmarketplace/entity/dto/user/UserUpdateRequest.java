package com.uade.tpo.foodmarketplace.entity.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Contiene los datos de perfil que pueden editarse sin cambiar el rol.
 */
@Data
public class UserUpdateRequest {

    @NotBlank
    private String nombre;

    @NotBlank
    private String apellido;

    @NotBlank
    @Email
    private String email;
}
