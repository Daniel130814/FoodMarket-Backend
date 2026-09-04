package com.uade.tpo.foodmarketplace.exceptions.chefprofile;

/**
 * Indica que el perfil de chef solicitado no existe.
 */
public class ChefProfileNotFoundException extends RuntimeException {

    public ChefProfileNotFoundException() {
        super("El perfil de chef indicado no existe");
    }
}
