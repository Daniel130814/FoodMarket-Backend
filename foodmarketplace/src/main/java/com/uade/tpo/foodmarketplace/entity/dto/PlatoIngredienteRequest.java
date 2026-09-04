package com.uade.tpo.foodmarketplace.entity.dto;

import java.math.BigDecimal;
import com.uade.tpo.foodmarketplace.entity.UnidadMedida;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PlatoIngredienteRequest {
    @NotNull
    private Long ingredienteId;

    @NotNull
    @Positive
    private BigDecimal cantidad;

    @NotNull
    private UnidadMedida unidadMedida;
}
