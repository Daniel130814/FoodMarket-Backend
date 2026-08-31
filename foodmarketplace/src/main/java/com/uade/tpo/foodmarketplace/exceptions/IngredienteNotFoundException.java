package com.uade.tpo.foodmarketplace.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "El ingrediente indicado no existe")
public class IngredienteNotFoundException extends RuntimeException {

    public IngredienteNotFoundException() {
        super("El ingrediente indicado no existe");
    }
}
