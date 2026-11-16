package com.uade.tpo.foodmarketplace.exceptions.favorito;

public class FavoritoNotFoundException extends RuntimeException {

    public FavoritoNotFoundException() {
        super("El plato no está en tus favoritos");
    }
}
