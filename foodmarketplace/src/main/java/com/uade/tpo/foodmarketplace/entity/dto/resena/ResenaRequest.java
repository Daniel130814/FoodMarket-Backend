package com.uade.tpo.foodmarketplace.entity.dto.resena;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResenaRequest {
    @NotNull
    @Min(1)
    @Max(5)
    private Integer calificacion;

    private String comentario;

    @NotNull
    private Long platoId;
}
