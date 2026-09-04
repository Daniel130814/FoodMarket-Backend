package com.uade.tpo.foodmarketplace.exceptions.order;

/**
 * Indica que la transición de estado solicitada para un subpedido no está permitida.
 */
public class InvalidSubPedidoStateException extends RuntimeException {

    public InvalidSubPedidoStateException(String message) {
        super(message);
    }
}
