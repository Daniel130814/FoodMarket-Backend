package com.uade.tpo.foodmarketplace.exceptions.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "El usuario indicado no existe")
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("El usuario indicado no existe");
    }
}
