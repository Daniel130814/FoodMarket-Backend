package com.uade.tpo.foodmarketplace.exceptions.domicilio;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "El domicilio indicado no existe")
public class DomicilioNotFoundException extends RuntimeException {

    public DomicilioNotFoundException() {
        super("El domicilio indicado no existe");
    }
}
