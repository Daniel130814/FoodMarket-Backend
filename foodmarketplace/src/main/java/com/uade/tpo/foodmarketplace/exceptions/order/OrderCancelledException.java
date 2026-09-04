package com.uade.tpo.foodmarketplace.exceptions.order;

/**
 * Indica que una orden cancelada no puede aceptar una nueva operación de pago.
 */
public class OrderCancelledException extends RuntimeException {

    public OrderCancelledException() {
        super("No se puede registrar un pago para una orden cancelada");
    }
}
