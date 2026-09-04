package com.uade.tpo.foodmarketplace.exceptions.resena;

/**
 * Indica que la reseña solicitada no existe.
 */
public class ResenaNotFoundException extends RuntimeException {

    public ResenaNotFoundException() {
        super("La resena indicada no existe");
    }
}
