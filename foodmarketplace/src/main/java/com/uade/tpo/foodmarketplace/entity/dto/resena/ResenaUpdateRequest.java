package com.uade.tpo.foodmarketplace.entity.dto.resena;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Contiene los campos que pueden corregirse en una reseña existente.
 */
@Data
public class ResenaUpdateRequest {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer calificacion;

    private String comentario;
}
