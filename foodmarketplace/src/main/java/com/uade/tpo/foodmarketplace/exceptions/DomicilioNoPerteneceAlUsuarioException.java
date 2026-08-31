package com.uade.tpo.foodmarketplace.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "El domicilio no pertenece al usuario indicado")
public class DomicilioNoPerteneceAlUsuarioException extends RuntimeException {

    public DomicilioNoPerteneceAlUsuarioException() {
        super("El domicilio no pertenece al usuario indicado");
    }
}
