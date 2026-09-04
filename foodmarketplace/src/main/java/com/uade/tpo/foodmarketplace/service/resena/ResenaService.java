package com.uade.tpo.foodmarketplace.service.resena;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.foodmarketplace.entity.resena.Resena;

public interface ResenaService {

    List<Resena> getResenas();

    Optional<Resena> getResenaById(Long resenaId);

    List<Resena> getResenasByPlatoId(Long platoId);

    Resena createResena(Integer calificacion, String comentario, Long clienteId, Long platoId);
}
