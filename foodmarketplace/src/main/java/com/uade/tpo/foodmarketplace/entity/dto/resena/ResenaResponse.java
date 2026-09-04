package com.uade.tpo.foodmarketplace.entity.dto.resena;

import java.time.LocalDateTime;

public record ResenaResponse(
        Long id,
        Integer calificacion,
        String comentario,
        LocalDateTime fechaCreacion,
        Long clienteId,
        Long platoId) {
}
