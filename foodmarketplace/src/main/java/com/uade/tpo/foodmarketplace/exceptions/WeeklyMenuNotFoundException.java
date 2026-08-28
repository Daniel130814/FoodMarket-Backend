package com.uade.tpo.foodmarketplace.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "El menu semanal indicado no existe")
public class WeeklyMenuNotFoundException extends RuntimeException {

    public WeeklyMenuNotFoundException() {
        super("El menu semanal indicado no existe");
    }
}
