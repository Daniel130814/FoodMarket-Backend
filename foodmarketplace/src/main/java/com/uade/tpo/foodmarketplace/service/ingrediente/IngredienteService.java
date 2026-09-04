package com.uade.tpo.foodmarketplace.service.ingrediente;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.foodmarketplace.entity.ingrediente.Ingrediente;

public interface IngredienteService {

    List<Ingrediente> getIngredientes();

    Optional<Ingrediente> getIngredienteById(Long ingredienteId);

    Ingrediente createIngrediente(String nombre, String descripcion);
}
