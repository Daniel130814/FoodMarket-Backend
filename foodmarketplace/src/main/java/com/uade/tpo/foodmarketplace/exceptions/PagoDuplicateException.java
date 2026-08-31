package com.uade.tpo.foodmarketplace.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "El pedido indicado ya posee un pago")
public class PagoDuplicateException extends RuntimeException {

    public PagoDuplicateException() {
        super("El pedido indicado ya posee un pago");
    }
}
