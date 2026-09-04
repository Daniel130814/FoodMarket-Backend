package com.uade.tpo.foodmarketplace.exceptions.order;

/**
 * Indica que una operación es incompatible con el estado actual de la orden.
 */
public class InvalidOrderStateException extends RuntimeException {

    public InvalidOrderStateException(String message) {
        super(message);
    }
}
