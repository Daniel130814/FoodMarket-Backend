package com.uade.tpo.foodmarketplace.entity.dto.resena;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Contains the fields that can be corrected in an existing review.
 */
@Data
public class ResenaUpdateRequest {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer calificacion;

    private String comentario;
}
