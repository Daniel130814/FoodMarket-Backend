package com.uade.tpo.foodmarketplace.exceptions.category;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Ya existe una categoría con ese nombre")
public class CategoryDuplicateException extends RuntimeException {

    public CategoryDuplicateException() {
        super("Ya existe una categoría con ese nombre");
    }
}
