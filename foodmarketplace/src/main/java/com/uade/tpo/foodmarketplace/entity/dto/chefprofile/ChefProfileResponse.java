package com.uade.tpo.foodmarketplace.entity.dto.chefprofile;
import java.math.BigDecimal;

public record ChefProfileResponse(
        Long id,
        Long userId,
        String nombre,
        String apellido,
        String biografia,
        String especialidad,
        String nacionalidad,
        String fotoUrl,
        String descripcion,
        BigDecimal reputacion) {
}
