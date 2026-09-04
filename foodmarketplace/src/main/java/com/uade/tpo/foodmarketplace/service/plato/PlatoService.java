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
     * Actualiza los datos editables, categorías e ingredientes de un plato existente.
     */
    Plato updatePlato(Long platoId, PlatoRequest request);

    /**
     * Elimina un plato o lo pausa cuando los registros históricos aún lo referencian.
     */
    void deletePlato(Long platoId);
}
