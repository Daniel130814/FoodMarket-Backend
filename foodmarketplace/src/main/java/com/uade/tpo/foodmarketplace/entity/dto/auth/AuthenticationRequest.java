package com.uade.tpo.foodmarketplace.entity.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(
        @NotBlank String username,
        @NotBlank String password) {

    @Override
    public String toString() {
        return "AuthenticationRequest[username=" + username + ", password=[REDACTED]]";
    }
}
