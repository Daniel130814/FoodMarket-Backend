package com.uade.tpo.foodmarketplace.exceptions.order;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "El pedido indicado no existe")
public class PedidoNotFoundException extends RuntimeException {

    public PedidoNotFoundException() {
        super("El pedido indicado no existe");
    }
}
