package com.uade.tpo.foodmarketplace.entity.dto;

import java.util.List;

import com.uade.tpo.foodmarketplace.entity.DiaSemana;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatoRequest {
    private String nombre;
    private String descripcion;
    private List<Long> ingredientesIds;
    private DiaSemana diaSemana;
    private String imagenUrl;
    private Long menuSemanalId;
}
