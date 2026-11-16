package com.uade.tpo.foodmarketplace.service.favorito;

import java.util.List;

import com.uade.tpo.foodmarketplace.entity.plato.Plato;

public interface FavoritoService {

    /** Platos favoritos visibles del cliente autenticado, del más nuevo al más viejo. */
    List<Plato> getFavoritos();

    /** Agrega un plato visible a los favoritos del cliente autenticado. */
    Plato agregar(Long platoId);

    /** Quita un plato de los favoritos del cliente autenticado. */
    void quitar(Long platoId);
}
