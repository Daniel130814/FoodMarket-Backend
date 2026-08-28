package com.uade.tpo.foodmarketplace.entity.dto;

import com.uade.tpo.foodmarketplace.entity.DiaSemana;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DishRequest {
    private String nombre;
    private String descripcion;
    private String ingredientes;
    private DiaSemana diaSemana;
    private String imagenUrl;
    private Long menuSemanalId;
}
