package com.uade.tpo.foodmarketplace.entity.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(
        @NotBlank @Email String email,
        @NotBlank String password) {

    @Override
    public String toString() {
        return "AuthenticationRequest[email=" + email + ", password=[REDACTED]]";
    }
}
