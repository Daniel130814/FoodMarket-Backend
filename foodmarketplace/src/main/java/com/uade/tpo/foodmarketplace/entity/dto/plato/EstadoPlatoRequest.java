package com.uade.tpo.foodmarketplace.entity.dto.plato;

import com.uade.tpo.foodmarketplace.entity.plato.EstadoPlato;

import jakarta.validation.constraints.NotNull;

/** Cambia solo el estado de un plato sin reenviar el plato completo. */
public record EstadoPlatoRequest(@NotNull EstadoPlato estado) {
}
