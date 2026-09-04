package com.uade.tpo.foodmarketplace.entity.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.uade.tpo.foodmarketplace.entity.EstadoPago;
import com.uade.tpo.foodmarketplace.entity.MedioPago;

public record PagoResponse(
        Long id,
        Long pedidoId,
        BigDecimal monto,
        MedioPago medioPago,
        EstadoPago estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaPago) {
}
