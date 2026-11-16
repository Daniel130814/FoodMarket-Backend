package com.uade.tpo.foodmarketplace.entity.dto.plato;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Cambia solo el stock de un plato sin reenviar el plato completo. */
public record StockPlatoRequest(@NotNull @Min(0) Integer stockDisponible) {
}
