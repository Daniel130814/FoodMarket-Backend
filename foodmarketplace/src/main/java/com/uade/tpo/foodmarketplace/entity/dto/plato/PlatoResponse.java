package com.uade.tpo.foodmarketplace.entity.dto.plato;

import java.math.BigDecimal;
import java.util.List;
import com.uade.tpo.foodmarketplace.entity.plato.EstadoPlato;

public record PlatoResponse(
        Long id,
        String nombre,
        String descripcion,
        List<String> imagenesUrls,
        BigDecimal precio,
        BigDecimal descuentoPorcentaje,
        BigDecimal precioConDescuento,
        Integer stockDisponible,
        EstadoPlato estado,
        Long chefId,
        List<String> categorias,
        List<IngredientePlatoResponse> ingredientes) {
}
