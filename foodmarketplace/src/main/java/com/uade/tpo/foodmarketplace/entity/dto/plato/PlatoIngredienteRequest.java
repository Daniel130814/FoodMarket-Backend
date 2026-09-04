package com.uade.tpo.foodmarketplace.entity.dto.plato;

import java.math.BigDecimal;
import com.uade.tpo.foodmarketplace.entity.plato.UnidadMedida;
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
