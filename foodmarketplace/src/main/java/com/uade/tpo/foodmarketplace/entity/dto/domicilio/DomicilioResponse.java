package com.uade.tpo.foodmarketplace.entity.dto.domicilio;

public record DomicilioResponse(Long id, String calle, String numero, String piso, String departamento,
        String ciudad, String provincia, String codigoPostal, String indicacionesEntrega) {
}
