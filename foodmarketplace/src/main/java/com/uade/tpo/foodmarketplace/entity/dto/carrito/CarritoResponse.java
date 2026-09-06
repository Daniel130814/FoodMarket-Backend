package com.uade.tpo.foodmarketplace.entity.dto.carrito;

import java.math.BigDecimal;
import java.util.List;

public record CarritoResponse(Long id, List<ItemCarritoResponse> items, BigDecimal totalEstimado) {
}
