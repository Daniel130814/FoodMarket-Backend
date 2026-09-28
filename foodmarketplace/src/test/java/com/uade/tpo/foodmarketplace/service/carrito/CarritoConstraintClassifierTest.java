package com.uade.tpo.foodmarketplace.service.carrito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class CarritoConstraintClassifierTest {

    @Test
    void reconoceSoloLaConstraintExactaDelCliente() {
        assertTrue(CarritoConstraintClassifier.esClienteUnico(conConstraint("uk_carrito_cliente")));
        assertFalse(CarritoConstraintClassifier.esClienteUnico(conConstraint("fk_carritos_cliente")));
    }

    @Test
    void reconoceSoloLaConstraintExactaDelItem() {
        assertTrue(CarritoConstraintClassifier.esItemUnico(conConstraint("uk_item_carrito_plato")));
        assertFalse(CarritoConstraintClassifier.esItemUnico(conConstraint("fk_items_carrito_plato")));
    }

    @Test
    void noReconoceTextoGenericoNiConstraintDesconocida() {
        assertFalse(CarritoConstraintClassifier.esClienteUnico(
                new DataIntegrityViolationException("error en carritos cliente")));
        assertFalse(CarritoConstraintClassifier.esItemUnico(
                new DataIntegrityViolationException("Duplicate entry for key otra_constraint")));
    }

    @Test
    void usaElNombreExactoComoFallbackCuandoElProveedorNoLoExpone() {
        assertTrue(CarritoConstraintClassifier.esClienteUnico(
                new DataIntegrityViolationException("Duplicate entry for key uk_carrito_cliente")));
    }

    private DataIntegrityViolationException conConstraint(String nombre) {
        return new DataIntegrityViolationException("integrity",
                new ConstraintViolationException("constraint", new SQLException(), "insert", nombre));
    }
}
