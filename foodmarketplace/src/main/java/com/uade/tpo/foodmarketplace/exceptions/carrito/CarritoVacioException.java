package com.uade.tpo.foodmarketplace.exceptions.carrito;

public class CarritoVacioException extends RuntimeException {
    public CarritoVacioException() {
        super("El carrito está vacío");
    }
}
