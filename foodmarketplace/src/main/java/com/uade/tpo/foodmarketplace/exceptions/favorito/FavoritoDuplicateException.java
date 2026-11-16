package com.uade.tpo.foodmarketplace.exceptions.favorito;

public class FavoritoDuplicateException extends RuntimeException {

    public FavoritoDuplicateException() {
        super("El plato ya está en tus favoritos");
    }
}
