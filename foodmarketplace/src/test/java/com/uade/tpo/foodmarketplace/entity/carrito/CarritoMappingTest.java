package com.uade.tpo.foodmarketplace.entity.carrito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jakarta.persistence.Table;

class CarritoMappingTest {

    @Test
    void declaraLaUnicidadDelClienteConNombreEstable() {
        assertTrue(tieneConstraint(Carrito.class.getAnnotation(Table.class), "uk_carrito_cliente"));
    }

    @Test
    void declaraLaUnicidadDelItemConNombreEstable() {
        Table tabla = ItemCarrito.class.getAnnotation(Table.class);
        assertTrue(tieneConstraint(tabla, "uk_item_carrito_plato"));
        assertEquals(2, tabla.uniqueConstraints()[0].columnNames().length);
    }

    private boolean tieneConstraint(Table tabla, String nombre) {
        return java.util.Arrays.stream(tabla.uniqueConstraints()).anyMatch(constraint -> nombre.equals(constraint.name()));
    }
}
