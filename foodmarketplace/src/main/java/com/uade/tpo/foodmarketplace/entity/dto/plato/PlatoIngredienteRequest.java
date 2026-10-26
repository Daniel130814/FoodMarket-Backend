package com.uade.tpo.foodmarketplace.entity.dto.plato;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uade.tpo.foodmarketplace.entity.ingrediente.Ingrediente;
import com.uade.tpo.foodmarketplace.entity.plato.UnidadMedida;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Un ingrediente de la receta: se identifica por ingredienteId o por nombre, nunca por ambos. */
@Data
public class PlatoIngredienteRequest {
    private Long ingredienteId;

    @Size(max = Ingrediente.NOMBRE_MAX_LENGTH, message = "El nombre del ingrediente no puede superar los 255 caracteres")
    private String nombre;

    @NotNull
    @Positive
    private BigDecimal cantidad;

    @NotNull
    private UnidadMedida unidadMedida;

    @JsonIgnore
    @AssertTrue(message = "Cada ingrediente debe indicar exactamente uno de: ingredienteId o nombre")
    public boolean isIdentificacionValida() {
        return (ingredienteId != null) != (nombre != null);
    }

    @JsonIgnore
    @AssertTrue(message = "El nombre del ingrediente no puede estar vacio")
    public boolean isNombreNoVacio() {
        return nombre == null || !nombre.isBlank();
    }
}
