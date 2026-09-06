package com.uade.tpo.foodmarketplace.entity.dto.carrito;

import jakarta.validation.constraints.NotNull;

public record CheckoutCarritoRequest(@NotNull Long domicilioEntregaId) {
}
