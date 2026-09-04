package com.uade.tpo.foodmarketplace.entity.dto.chefprofile;
import java.math.BigDecimal;

public record ChefProfileResponse(
        Long id,
        Long userId,
        String biografia,
        String especialidad,
        String fotoUrl,
        String descripcion,
        BigDecimal reputacion) {
}
