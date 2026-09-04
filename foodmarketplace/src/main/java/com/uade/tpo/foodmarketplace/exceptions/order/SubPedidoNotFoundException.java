package com.uade.tpo.foodmarketplace.exceptions.order;

/**
 * Indica que el subpedido de chef solicitado no existe.
 */
public class SubPedidoNotFoundException extends RuntimeException {

    public SubPedidoNotFoundException() {
        super("El subpedido indicado no existe");
    }
}
