package com.uade.tpo.foodmarketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.tpo.foodmarketplace.entity.WeeklyMenu;

public interface WeeklyMenuRepository extends JpaRepository<WeeklyMenu, Long> {
}
