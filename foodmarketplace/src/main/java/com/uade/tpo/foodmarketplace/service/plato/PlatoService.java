package com.uade.tpo.foodmarketplace.service.plato;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.foodmarketplace.entity.plato.Plato;
import com.uade.tpo.foodmarketplace.entity.dto.plato.PlatoRequest;

public interface PlatoService {

    List<Plato> getPlatos();

    Optional<Plato> getPlatoById(Long platoId);

    Plato createPlato(PlatoRequest request);

    /**
     * Updates the editable data, categories, and ingredients of an existing dish.
     */
    Plato updatePlato(Long platoId, PlatoRequest request);

    /**
     * Deletes a dish or pauses it when historical records still reference it.
     */
    void deletePlato(Long platoId);
}
