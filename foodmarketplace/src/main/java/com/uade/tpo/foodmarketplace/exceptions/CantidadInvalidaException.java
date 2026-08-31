package com.uade.tpo.foodmarketplace.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "La cantidad debe ser mayor que cero")
public class CantidadInvalidaException extends RuntimeException {

    public CantidadInvalidaException() {
        super("La cantidad debe ser mayor que cero");
    }
}
