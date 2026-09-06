package com.uade.tpo.foodmarketplace.exceptions.carrito;

public class ItemCarritoNotFoundException extends RuntimeException {
    public ItemCarritoNotFoundException() {
        super("El item del carrito indicado no existe");
    }
}
