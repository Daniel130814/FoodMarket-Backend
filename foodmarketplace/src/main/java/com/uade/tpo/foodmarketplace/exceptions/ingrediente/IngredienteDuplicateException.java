package com.uade.tpo.foodmarketplace.exceptions.ingrediente;

/**
 * Indica que el nombre de un ingrediente debe ser único sin distinguir mayúsculas.
 */
public class IngredienteDuplicateException extends RuntimeException {

    public IngredienteDuplicateException() {
        super("Ya existe un ingrediente con ese nombre");
    }
}
