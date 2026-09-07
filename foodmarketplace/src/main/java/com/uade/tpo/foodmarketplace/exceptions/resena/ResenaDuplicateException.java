package com.uade.tpo.foodmarketplace.exceptions.resena;

public class ResenaDuplicateException extends RuntimeException {

    public static final String MESSAGE = "Ya realizaste una reseña para este plato.";

    public ResenaDuplicateException() {
        super(MESSAGE);
    }

    public ResenaDuplicateException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
