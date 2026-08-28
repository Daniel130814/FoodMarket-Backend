package com.uade.tpo.foodmarketplace.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.tpo.foodmarketplace.entity.Dish;

public interface DishRepository extends JpaRepository<Dish, Long> {

    List<Dish> findByMenuSemanalId(Long menuSemanalId);
}
