package com.uade.tpo.foodmarketplace.service;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.foodmarketplace.entity.DiaSemana;
import com.uade.tpo.foodmarketplace.entity.Dish;

public interface DishService {

    List<Dish> getDishes();

    Optional<Dish> getDishById(Long dishId);

    List<Dish> getDishesByMenuSemanalId(Long menuSemanalId);

    Dish createDish(String nombre, String descripcion, String ingredientes,
            DiaSemana diaSemana, String imagenUrl, Long menuSemanalId);
}
