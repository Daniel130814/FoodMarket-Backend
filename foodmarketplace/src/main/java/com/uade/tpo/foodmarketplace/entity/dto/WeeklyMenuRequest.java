package com.uade.tpo.foodmarketplace.entity.dto;

import java.time.LocalDate;

import com.uade.tpo.foodmarketplace.entity.EstadoMenu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyMenuRequest {
    private String nombre;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Float precio;
    private Integer stockDisponible;
    private EstadoMenu estado;
    private Long chefId;
    private Long categoriaId;
}
