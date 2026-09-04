package com.uade.tpo.foodmarketplace.exceptions.chefprofile;

/**
 * Signals that the requested chef profile does not exist.
 */
public class ChefProfileNotFoundException extends RuntimeException {

    public ChefProfileNotFoundException() {
        super("El perfil de chef indicado no existe");
    }
}
