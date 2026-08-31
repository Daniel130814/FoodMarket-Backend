package com.uade.tpo.foodmarketplace.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "El pago indicado no existe")
public class PagoNotFoundException extends RuntimeException {

    public PagoNotFoundException() {
        super("El pago indicado no existe");
    }
}
