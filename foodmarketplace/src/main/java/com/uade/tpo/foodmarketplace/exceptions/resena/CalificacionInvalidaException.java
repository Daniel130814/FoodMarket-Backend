package com.uade.tpo.foodmarketplace.exceptions.resena;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "La calificacion debe estar entre 1 y 5")
public class CalificacionInvalidaException extends RuntimeException {

    public CalificacionInvalidaException() {
        super("La calificacion debe estar entre 1 y 5");
    }
}
