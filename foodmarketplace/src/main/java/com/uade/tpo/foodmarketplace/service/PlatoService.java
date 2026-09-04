package com.uade.tpo.foodmarketplace.service;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.foodmarketplace.entity.Plato;
import com.uade.tpo.foodmarketplace.entity.dto.PlatoRequest;

public interface PlatoService {

    List<Plato> getPlatos();

    Optional<Plato> getPlatoById(Long platoId);

    Plato createPlato(PlatoRequest request);
}
