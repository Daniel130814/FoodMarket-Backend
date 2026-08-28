package com.uade.tpo.foodmarketplace.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.foodmarketplace.entity.Category;
import com.uade.tpo.foodmarketplace.entity.EstadoMenu;
import com.uade.tpo.foodmarketplace.entity.User;
import com.uade.tpo.foodmarketplace.entity.WeeklyMenu;
import com.uade.tpo.foodmarketplace.exceptions.CategoryNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.UserNotFoundException;
import com.uade.tpo.foodmarketplace.repository.CategoryRepository;
import com.uade.tpo.foodmarketplace.repository.UserRepository;
import com.uade.tpo.foodmarketplace.repository.WeeklyMenuRepository;

@Service
public class WeeklyMenuServiceImpl implements WeeklyMenuService {

    @Autowired
    private WeeklyMenuRepository weeklyMenuRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<WeeklyMenu> getWeeklyMenus() {
        return weeklyMenuRepository.findAll();
    }

    @Override
    public Optional<WeeklyMenu> getWeeklyMenuById(Long weeklyMenuId) {
        return weeklyMenuRepository.findById(weeklyMenuId);
    }

    @Override
    public WeeklyMenu createWeeklyMenu(String nombre, String descripcion, LocalDate fechaInicio,
            LocalDate fechaFin, Float precio, Integer stockDisponible, EstadoMenu estado,
            Long chefId, Long categoriaId) {

        User chef = userRepository.findById(chefId)
                .orElseThrow(UserNotFoundException::new);

        Category categoria = categoryRepository.findById(categoriaId)
                .orElseThrow(CategoryNotFoundException::new);

        WeeklyMenu weeklyMenu = new WeeklyMenu();
        weeklyMenu.setNombre(nombre);
        weeklyMenu.setDescripcion(descripcion);
        weeklyMenu.setFechaInicio(fechaInicio);
        weeklyMenu.setFechaFin(fechaFin);
        weeklyMenu.setPrecio(precio);
        weeklyMenu.setStockDisponible(stockDisponible);
        weeklyMenu.setEstado(estado == null ? EstadoMenu.BORRADOR : estado);
        weeklyMenu.setChef(chef);
        weeklyMenu.setCategoria(categoria);

        return weeklyMenuRepository.save(weeklyMenu);
    }
}
