package com.uade.tpo.foodmarketplace.entity.dto.plato;

import java.util.List;
import java.math.BigDecimal;
import com.uade.tpo.foodmarketplace.entity.plato.EstadoPlato;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatoRequest {
    @NotBlank
    private String nombre;

    private String descripcion;

    @NotNull
    @Positive
    private BigDecimal precio;

    @NotNull
    @PositiveOrZero
    private Integer stockDisponible;

    private EstadoPlato estado;
    private String imagenUrl;

    private List<Long> categoriasIds;

    @Valid
    private List<PlatoIngredienteRequest> ingredientes;
}
