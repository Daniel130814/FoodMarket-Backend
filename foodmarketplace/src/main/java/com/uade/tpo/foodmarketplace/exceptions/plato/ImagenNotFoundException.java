package com.uade.tpo.foodmarketplace.exceptions.plato;

public class ImagenNotFoundException extends RuntimeException {

    public ImagenNotFoundException() {
        super("La imagen indicada no existe");
    }
}
