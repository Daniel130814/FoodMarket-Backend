package com.uade.tpo.foodmarketplace.entity.dto.ingrediente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredienteRequest {
    @NotBlank
    private String nombre;

    @NotBlank
    private String descripcion;
}
