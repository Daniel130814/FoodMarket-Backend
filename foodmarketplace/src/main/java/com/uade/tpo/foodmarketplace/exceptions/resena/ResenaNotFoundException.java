package com.uade.tpo.foodmarketplace.exceptions.resena;

/**
 * Signals that the requested review does not exist.
 */
public class ResenaNotFoundException extends RuntimeException {

    public ResenaNotFoundException() {
        super("La resena indicada no existe");
    }
}
