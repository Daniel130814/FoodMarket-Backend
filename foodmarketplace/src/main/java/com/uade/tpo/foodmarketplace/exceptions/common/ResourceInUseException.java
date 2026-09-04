package com.uade.tpo.foodmarketplace.exceptions.common;

/**
 * Signals that a resource cannot be removed because another record references it.
 */
public class ResourceInUseException extends RuntimeException {

    public ResourceInUseException(String message) {
        super(message);
    }
}
