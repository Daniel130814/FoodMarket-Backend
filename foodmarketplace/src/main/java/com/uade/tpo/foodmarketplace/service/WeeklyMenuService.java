package com.uade.tpo.foodmarketplace.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.uade.tpo.foodmarketplace.entity.EstadoMenu;
import com.uade.tpo.foodmarketplace.entity.WeeklyMenu;

public interface WeeklyMenuService {

    List<WeeklyMenu> getWeeklyMenus();

    Optional<WeeklyMenu> getWeeklyMenuById(Long weeklyMenuId);

    WeeklyMenu createWeeklyMenu(String nombre, String descripcion, LocalDate fechaInicio,
            LocalDate fechaFin, Float precio, Integer stockDisponible, EstadoMenu estado,
            Long chefId, Long categoriaId);
}
