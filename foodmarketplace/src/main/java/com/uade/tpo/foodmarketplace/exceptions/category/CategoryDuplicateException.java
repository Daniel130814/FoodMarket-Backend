package com.uade.tpo.foodmarketplace.exceptions.category;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "La categoria que se intenta agregar esta duplicado")
public class CategoryDuplicateException extends RuntimeException {

    public CategoryDuplicateException() {
        super("La categoria que se intenta agregar esta duplicado");
    }
}
