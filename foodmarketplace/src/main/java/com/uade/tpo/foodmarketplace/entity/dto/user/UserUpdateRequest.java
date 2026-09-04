package com.uade.tpo.foodmarketplace.entity.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Contains user profile data that can be edited without changing the role.
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
