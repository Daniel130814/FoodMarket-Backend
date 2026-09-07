package com.uade.tpo.foodmarketplace.entity.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank String nombre,
        @NotBlank String apellido,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotNull TipoRegistro tipoCuenta) {

    @Override
    public String toString() {
        return "RegisterRequest[username=" + username + ", nombre=" + nombre + ", apellido=" + apellido
                + ", email=" + email + ", password=[REDACTED], tipoCuenta=" + tipoCuenta + "]";
    }
}
