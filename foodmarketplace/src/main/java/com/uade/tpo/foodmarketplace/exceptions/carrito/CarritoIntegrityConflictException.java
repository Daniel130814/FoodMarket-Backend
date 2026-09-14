package com.uade.tpo.foodmarketplace.exceptions.carrito;

/**
 * Indica que una carrera conocida de integridad del carrito no pudo recuperarse.
 */
public class CarritoIntegrityConflictException extends RuntimeException {

    public CarritoIntegrityConflictException(String message) {
        super(message);
    }
}
