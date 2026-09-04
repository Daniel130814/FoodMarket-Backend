package com.uade.tpo.foodmarketplace.exceptions.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "El usuario que se intenta agregar ya existe")
public class UserDuplicateException extends RuntimeException {

    public UserDuplicateException() {
        super("El usuario que se intenta agregar ya existe");
    }
}
