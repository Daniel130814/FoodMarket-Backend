package com.uade.tpo.foodmarketplace.exceptions.plato;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "El plato indicado no existe")
public class PlatoNotFoundException extends RuntimeException {

    public PlatoNotFoundException() {
        super("El plato indicado no existe");
    }
}
