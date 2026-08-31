package com.uade.tpo.foodmarketplace.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResenaRequest {
    private Integer calificacion;
    private String comentario;
    private Long clienteId;
    private Long platoId;
}
