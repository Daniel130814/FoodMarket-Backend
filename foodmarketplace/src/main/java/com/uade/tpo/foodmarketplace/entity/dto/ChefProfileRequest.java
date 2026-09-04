package com.uade.tpo.foodmarketplace.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChefProfileRequest {
    @NotNull
    private Long userId;

    private String biografia;
    private String especialidad;
    private String fotoUrl;
    private String descripcion;
}
