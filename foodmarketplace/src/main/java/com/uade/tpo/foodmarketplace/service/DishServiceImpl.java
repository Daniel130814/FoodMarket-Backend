package com.uade.tpo.foodmarketplace.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.foodmarketplace.entity.DiaSemana;
import com.uade.tpo.foodmarketplace.entity.Dish;
import com.uade.tpo.foodmarketplace.entity.WeeklyMenu;
import com.uade.tpo.foodmarketplace.exceptions.WeeklyMenuNotFoundException;
import com.uade.tpo.foodmarketplace.repository.DishRepository;
import com.uade.tpo.foodmarketplace.repository.WeeklyMenuRepository;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private WeeklyMenuRepository weeklyMenuRepository;

    @Override
    public List<Dish> getDishes() {
        return dishRepository.findAll();
    }

    @Override
    public Optional<Dish> getDishById(Long dishId) {
        return dishRepository.findById(dishId);
    }

    @Override
    public List<Dish> getDishesByMenuSemanalId(Long menuSemanalId) {
        if (!weeklyMenuRepository.existsById(menuSemanalId)) {
            throw new WeeklyMenuNotFoundException();
        }

        return dishRepository.findByMenuSemanalId(menuSemanalId);
    }

    @Override
    public Dish createDish(String nombre, String descripcion, String ingredientes,
            DiaSemana diaSemana, String imagenUrl, Long menuSemanalId) {

        WeeklyMenu menuSemanal = weeklyMenuRepository.findById(menuSemanalId)
                .orElseThrow(WeeklyMenuNotFoundException::new);

        Dish dish = new Dish();
        dish.setNombre(nombre);
        dish.setDescripcion(descripcion);
        dish.setIngredientes(ingredientes);
        dish.setDiaSemana(diaSemana);
        dish.setImagenUrl(imagenUrl);
        dish.setMenuSemanal(menuSemanal);

        return dishRepository.save(dish);
    }
}
