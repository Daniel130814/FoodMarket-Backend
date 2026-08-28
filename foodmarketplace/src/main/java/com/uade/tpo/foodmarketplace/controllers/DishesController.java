package com.uade.tpo.foodmarketplace.controllers;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.foodmarketplace.entity.Dish;
import com.uade.tpo.foodmarketplace.entity.dto.DishRequest;
import com.uade.tpo.foodmarketplace.exceptions.WeeklyMenuNotFoundException;
import com.uade.tpo.foodmarketplace.service.DishService;

@RestController
@RequestMapping("dishes")
public class DishesController {

    @Autowired
    private DishService dishService;

    @GetMapping
    public ResponseEntity<List<Dish>> getDishes() {
        return ResponseEntity.ok(dishService.getDishes());
    }

    @GetMapping("/{dishId}")
    public ResponseEntity<Dish> getDishById(@PathVariable("dishId") Long dishId) {
        Optional<Dish> dish = dishService.getDishById(dishId);

        if (dish.isPresent()) {
            return ResponseEntity.ok(dish.get());
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/weeklyMenu/{menuSemanalId}")
    public ResponseEntity<List<Dish>> getDishesByMenuSemanalId(
            @PathVariable("menuSemanalId") Long menuSemanalId) {
        return ResponseEntity.ok(dishService.getDishesByMenuSemanalId(menuSemanalId));
    }

    @PostMapping("createDish")
    public ResponseEntity<Dish> createDish(@RequestBody DishRequest dishRequest)
            throws WeeklyMenuNotFoundException {
        Dish result = dishService.createDish(
                dishRequest.getNombre(),
                dishRequest.getDescripcion(),
                dishRequest.getIngredientes(),
                dishRequest.getDiaSemana(),
                dishRequest.getImagenUrl(),
                dishRequest.getMenuSemanalId());

        return ResponseEntity
                .created(URI.create("/dishes/" + result.getId()))
                .body(result);
    }
}
