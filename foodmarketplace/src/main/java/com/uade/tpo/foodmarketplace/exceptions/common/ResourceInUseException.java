package com.uade.tpo.foodmarketplace.exceptions.common;

/**
 * Indica que un recurso no puede eliminarse porque otro registro lo referencia.
 */
public class ResourceInUseException extends RuntimeException {

    public ResourceInUseException(String message) {
        super(message);
    }
}
