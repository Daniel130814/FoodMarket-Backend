package com.uade.tpo.foodmarketplace.entity.dto;

import java.math.BigDecimal;

public record DetallePedidoResponse(
        Long id,
        Long platoId,
        String platoNombre,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal) {
}
