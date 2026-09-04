package com.uade.tpo.foodmarketplace.exceptions.ingrediente;

/**
 * Signals that an ingredient name must be unique regardless of letter case.
 */
public class IngredienteDuplicateException extends RuntimeException {

    public IngredienteDuplicateException() {
        super("Ya existe un ingrediente con ese nombre");
    }
}
