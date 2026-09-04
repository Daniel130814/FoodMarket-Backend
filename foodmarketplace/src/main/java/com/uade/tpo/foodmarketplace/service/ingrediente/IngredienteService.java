package com.uade.tpo.foodmarketplace.service.ingrediente;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.foodmarketplace.entity.ingrediente.Ingrediente;

public interface IngredienteService {

    List<Ingrediente> getIngredientes();

    Optional<Ingrediente> getIngredienteById(Long ingredienteId);

    Ingrediente createIngrediente(String nombre, String descripcion);

    /**
     * Updates the name and description of an existing ingredient.
     */
    Ingrediente updateIngrediente(Long ingredienteId, String nombre, String descripcion);

    /**
     * Deletes an ingredient when it is not part of a dish recipe.
     */
    void deleteIngrediente(Long ingredienteId);
}
