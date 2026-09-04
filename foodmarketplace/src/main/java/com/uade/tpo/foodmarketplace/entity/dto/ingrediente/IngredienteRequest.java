package com.uade.tpo.foodmarketplace.entity.dto.ingrediente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredienteRequest {
    private String nombre;
    private String descripcion;
}
