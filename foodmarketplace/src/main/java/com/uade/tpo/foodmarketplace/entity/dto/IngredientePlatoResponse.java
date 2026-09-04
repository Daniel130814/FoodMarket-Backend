package com.uade.tpo.foodmarketplace.entity.dto;

import java.math.BigDecimal;

import com.uade.tpo.foodmarketplace.entity.UnidadMedida;

public record IngredientePlatoResponse(
        Long ingredienteId,
        String nombre,
        BigDecimal cantidad,
        UnidadMedida unidadMedida) {
}
