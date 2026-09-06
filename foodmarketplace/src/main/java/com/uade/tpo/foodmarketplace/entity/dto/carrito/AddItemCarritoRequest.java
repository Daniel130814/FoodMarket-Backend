package com.uade.tpo.foodmarketplace.entity.dto.carrito;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddItemCarritoRequest(@NotNull Long platoId, @NotNull @Positive Integer cantidad) {
}
