package com.uade.tpo.foodmarketplace.entity.dto.plato;

import java.math.BigDecimal;

import com.uade.tpo.foodmarketplace.entity.plato.UnidadMedida;

public record IngredientePlatoResponse(
        Long ingredienteId,
        String nombre,
        BigDecimal cantidad,
        UnidadMedida unidadMedida) {
}
