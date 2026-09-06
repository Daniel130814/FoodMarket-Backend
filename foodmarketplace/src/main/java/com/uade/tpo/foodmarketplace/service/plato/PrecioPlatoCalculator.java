package com.uade.tpo.foodmarketplace.service.plato;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.uade.tpo.foodmarketplace.entity.plato.Plato;

/** Centraliza el cálculo del precio efectivo para catálogo, carrito y órdenes. */
public final class PrecioPlatoCalculator {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private PrecioPlatoCalculator() {
    }

    /**
     * Calcula el precio base menos el descuento actual sin persistir un segundo precio.
     */
    public static BigDecimal precioEfectivo(Plato plato) {
        BigDecimal descuento = plato.getDescuentoPorcentaje() == null ? BigDecimal.ZERO : plato.getDescuentoPorcentaje();
        return plato.getPrecio().multiply(CIEN.subtract(descuento)).divide(CIEN, 2, RoundingMode.HALF_UP);
    }
}
