package com.uade.tpo.foodmarketplace.service.carrito;

import org.springframework.dao.DataIntegrityViolationException;

/**
 * Reconoce únicamente las constraints de unicidad conocidas del carrito.
 */
final class CarritoConstraintClassifier {

    static final String UK_CARRITO_CLIENTE = "uk_carrito_cliente";
    static final String UK_ITEM_CARRITO_PLATO = "uk_item_carrito_plato";

    private CarritoConstraintClassifier() {
    }

    static boolean esClienteUnico(DataIntegrityViolationException exception) {
        return tieneConstraint(exception, UK_CARRITO_CLIENTE);
    }

    static boolean esItemUnico(DataIntegrityViolationException exception) {
        return tieneConstraint(exception, UK_ITEM_CARRITO_PLATO);
    }

    private static boolean tieneConstraint(Throwable exception, String nombreEsperado) {
        Throwable actual = exception;
        while (actual != null) {
            if (actual instanceof org.hibernate.exception.ConstraintViolationException constraintViolation) {
                String constraintName = constraintViolation.getConstraintName();
                if (nombreEsperado.equals(constraintName)) {
                    return true;
                }
                if (constraintName == null && contieneNombreExacto(actual.getMessage(), nombreEsperado)) {
                    return true;
                }
            } else if (contieneNombreExacto(actual.getMessage(), nombreEsperado)) {
                // Algunos drivers omiten ConstraintViolationException, pero conservan el nombre
                // declarado.
                return true;
            }
            actual = actual.getCause();
        }
        return false;
    }

    private static boolean contieneNombreExacto(String message, String nombreEsperado) {
        return message != null && message.contains(nombreEsperado);
    }
}
