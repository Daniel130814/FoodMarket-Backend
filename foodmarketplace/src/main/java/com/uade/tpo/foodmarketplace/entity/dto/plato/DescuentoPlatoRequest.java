package com.uade.tpo.foodmarketplace.entity.dto.plato;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** Recibe exclusivamente el porcentaje de descuento que gestiona el chef propietario. */
public record DescuentoPlatoRequest(
        @NotNull @DecimalMin("0.0") @DecimalMax(value = "99.99") BigDecimal porcentaje) {
}
