package com.uade.tpo.foodmarketplace.service;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.foodmarketplace.entity.Ingrediente;

public interface IngredienteService {

    List<Ingrediente> getIngredientes();

    Optional<Ingrediente> getIngredienteById(Long ingredienteId);

    Ingrediente createIngrediente(String nombre, String descripcion);
}
