package com.uade.tpo.foodmarketplace.exceptions.resena;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "El cliente ya realizo una resena para este menu semanal")
public class ResenaDuplicateException extends RuntimeException {

    public ResenaDuplicateException() {
        super("El cliente ya realizo una resena para este menu semanal");
    }
}
