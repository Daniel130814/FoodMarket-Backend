package com.uade.tpo.foodmarketplace.exceptions.pago;

/**
 * Indica que la transición de estado solicitada para un pago no está permitida.
 */
public class InvalidPagoStateException extends RuntimeException {

    public InvalidPagoStateException(String message) {
        super(message);
    }
}
