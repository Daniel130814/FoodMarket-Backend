package com.uade.tpo.foodmarketplace.service.plato;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.uade.tpo.foodmarketplace.entity.plato.Plato;

class PrecioPlatoCalculatorTest {

    @Test
    void calculaPrecioEfectivoConDescuento() {
        Plato plato = plato(new BigDecimal("10000.00"), new BigDecimal("20.00"));

        assertEquals(new BigDecimal("8000.00"), PrecioPlatoCalculator.precioEfectivo(plato));
    }

    @Test
    void conservaPrecioBaseCuandoElDescuentoEsCero() {
        Plato plato = plato(new BigDecimal("10000.00"), BigDecimal.ZERO);

        assertEquals(new BigDecimal("10000.00"), PrecioPlatoCalculator.precioEfectivo(plato));
    }

    private Plato plato(BigDecimal precio, BigDecimal descuento) {
        Plato plato = new Plato();
        plato.setPrecio(precio);
        plato.setDescuentoPorcentaje(descuento);
        return plato;
    }
}
