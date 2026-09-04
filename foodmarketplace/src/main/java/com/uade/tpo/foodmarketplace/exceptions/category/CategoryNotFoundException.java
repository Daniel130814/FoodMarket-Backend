package com.uade.tpo.foodmarketplace.exceptions.category;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "La categoria indicada no existe")
public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException() {
        super("La categoria indicada no existe");
    }
}
