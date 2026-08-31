package com.uade.tpo.foodmarketplace.service;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.foodmarketplace.entity.DiaSemana;
import com.uade.tpo.foodmarketplace.entity.Plato;

public interface PlatoService {

    List<Plato> getPlatos();

    Optional<Plato> getPlatoById(Long platoId);

    List<Plato> getPlatosByMenuSemanalId(Long menuSemanalId);

    Plato createPlato(String nombre, String descripcion, List<Long> ingredientesIds,
            DiaSemana diaSemana, String imagenUrl, Long menuSemanalId);
}
