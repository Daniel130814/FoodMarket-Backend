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

import com.uade.tpo.foodmarketplace.entity.WeeklyMenu;
import com.uade.tpo.foodmarketplace.entity.dto.WeeklyMenuRequest;
import com.uade.tpo.foodmarketplace.exceptions.CategoryNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.UserNotFoundException;
import com.uade.tpo.foodmarketplace.service.WeeklyMenuService;

@RestController
@RequestMapping("weeklyMenus")
public class WeeklyMenusController {

    @Autowired
    private WeeklyMenuService weeklyMenuService;

    @GetMapping
    public ResponseEntity<List<WeeklyMenu>> getWeeklyMenus() {
        return ResponseEntity.ok(weeklyMenuService.getWeeklyMenus());
    }

    @GetMapping("/{weeklyMenuId}")
    public ResponseEntity<WeeklyMenu> getWeeklyMenuById(
            @PathVariable("weeklyMenuId") Long weeklyMenuId) {
        Optional<WeeklyMenu> weeklyMenu = weeklyMenuService.getWeeklyMenuById(weeklyMenuId);

        if (weeklyMenu.isPresent()) {
            return ResponseEntity.ok(weeklyMenu.get());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("createWeeklyMenu")
    public ResponseEntity<WeeklyMenu> createWeeklyMenu(@RequestBody WeeklyMenuRequest weeklyMenuRequest)
            throws UserNotFoundException, CategoryNotFoundException {
        WeeklyMenu result = weeklyMenuService.createWeeklyMenu(
                weeklyMenuRequest.getNombre(),
                weeklyMenuRequest.getDescripcion(),
                weeklyMenuRequest.getFechaInicio(),
                weeklyMenuRequest.getFechaFin(),
                weeklyMenuRequest.getPrecio(),
                weeklyMenuRequest.getStockDisponible(),
                weeklyMenuRequest.getEstado(),
                weeklyMenuRequest.getChefId(),
                weeklyMenuRequest.getCategoriaId());

        return ResponseEntity
                .created(URI.create("/weeklyMenus/" + result.getId()))
                .body(result);
    }
}
