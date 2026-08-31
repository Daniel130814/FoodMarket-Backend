package com.uade.tpo.foodmarketplace.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.foodmarketplace.entity.DiaSemana;
import com.uade.tpo.foodmarketplace.entity.Ingrediente;
import com.uade.tpo.foodmarketplace.entity.Plato;
import com.uade.tpo.foodmarketplace.entity.WeeklyMenu;
import com.uade.tpo.foodmarketplace.exceptions.IngredienteNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.WeeklyMenuNotFoundException;
import com.uade.tpo.foodmarketplace.repository.IngredienteRepository;
import com.uade.tpo.foodmarketplace.repository.PlatoRepository;
import com.uade.tpo.foodmarketplace.repository.WeeklyMenuRepository;

@Service
public class PlatoServiceImpl implements PlatoService {

    @Autowired
    private PlatoRepository platoRepository;

    @Autowired
    private WeeklyMenuRepository weeklyMenuRepository;

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Override
    public List<Plato> getPlatos() {
        return platoRepository.findAll();
    }

    @Override
    public Optional<Plato> getPlatoById(Long platoId) {
        return platoRepository.findById(platoId);
    }

    @Override
    public List<Plato> getPlatosByMenuSemanalId(Long menuSemanalId) {
        if (!weeklyMenuRepository.existsById(menuSemanalId)) {
            throw new WeeklyMenuNotFoundException();
        }

        return platoRepository.findByMenuSemanalId(menuSemanalId);
    }

    @Override
    public Plato createPlato(String nombre, String descripcion, List<Long> ingredientesIds,
            DiaSemana diaSemana, String imagenUrl, Long menuSemanalId) {

        WeeklyMenu menuSemanal = weeklyMenuRepository.findById(menuSemanalId)
                .orElseThrow(WeeklyMenuNotFoundException::new);

        List<Ingrediente> ingredientes = ingredientesIds.stream()
                .map(ingredienteId -> ingredienteRepository.findById(ingredienteId)
                        .orElseThrow(IngredienteNotFoundException::new))
                .toList();

        Plato plato = new Plato();
        plato.setNombre(nombre);
        plato.setDescripcion(descripcion);
        plato.setIngredientes(ingredientes);
        plato.setDiaSemana(diaSemana);
        plato.setImagenUrl(imagenUrl);
        plato.setMenuSemanal(menuSemanal);

        return platoRepository.save(plato);
    }
}
