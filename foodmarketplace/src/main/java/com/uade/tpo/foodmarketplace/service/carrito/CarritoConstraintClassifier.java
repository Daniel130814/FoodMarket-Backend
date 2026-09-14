package com.uade.tpo.foodmarketplace.service.carrito;

import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;

/**
 * Reconoce únicamente las constraints de unicidad conocidas del carrito.
 */
final class CarritoConstraintClassifier {

    private CarritoConstraintClassifier() {
    }

    static boolean esClienteUnico(DataIntegrityViolationException exception) {
        String detalle = detalle(exception);
        return detalle.contains("carritos") && detalle.contains("cliente")
                || detalle.contains("uk_carritos_cliente")
                || detalle.contains("carritos_cliente_id");
    }

    static boolean esItemUnico(DataIntegrityViolationException exception) {
        String detalle = detalle(exception);
        return detalle.contains("items_carrito") && detalle.contains("carrito") && detalle.contains("plato")
                || detalle.contains("uk_items_carrito")
                || detalle.contains("items_carrito_carrito_id_plato_id");
    }

    private static String detalle(Throwable exception) {
        StringBuilder detalle = new StringBuilder();
        Throwable actual = exception;
        while (actual != null) {
            if (actual.getMessage() != null) {
                detalle.append(actual.getMessage()).append(' ');
            }
            if (actual instanceof org.hibernate.exception.ConstraintViolationException constraintViolation
                    && constraintViolation.getConstraintName() != null) {
                detalle.append(constraintViolation.getConstraintName()).append(' ');
            }
            actual = actual.getCause();
        }
        return detalle.toString().toLowerCase(Locale.ROOT);
    }
}
