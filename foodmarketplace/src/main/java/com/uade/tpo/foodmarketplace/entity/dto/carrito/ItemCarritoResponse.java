package com.uade.tpo.foodmarketplace.entity.dto.carrito;

import java.math.BigDecimal;

public record ItemCarritoResponse(Long id, Long platoId, String nombrePlato, Integer cantidad,
        BigDecimal precioBase, BigDecimal descuentoPorcentaje, BigDecimal precioUnitarioActual,
        BigDecimal subtotalEstimado) {
}
