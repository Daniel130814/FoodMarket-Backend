package com.uade.tpo.foodmarketplace.entity.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambioPasswordRequest(
        @NotBlank String actual,
        @NotBlank @Size(min = 8) String nueva) {

    @Override
    public String toString() {
        return "CambioPasswordRequest[actual=[REDACTED], nueva=[REDACTED]]";
    }
}
