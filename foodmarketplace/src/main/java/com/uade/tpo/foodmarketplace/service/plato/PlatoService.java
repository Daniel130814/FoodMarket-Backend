package com.uade.tpo.foodmarketplace.service.plato;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.uade.tpo.foodmarketplace.entity.plato.Plato;
import com.uade.tpo.foodmarketplace.entity.dto.plato.PlatoRequest;

public interface PlatoService {

    List<Plato> getPlatos(String nombre, Long categoriaId, BigDecimal precioMin, BigDecimal precioMax);

    /**
     * Devuelve platos públicos; BORRADOR y PAUSADO solo si el usuario autenticado es el chef dueño o ADMIN.
     */
    Optional<Plato> getPlatoById(Long platoId);

    /** Platos del chef autenticado en todos los estados. */
    List<Plato> getMisPlatos();

    /** Agrega una imagen subida por archivo al plato; solo el chef dueño puede hacerlo. */
    Plato agregarImagen(Long platoId, org.springframework.web.multipart.MultipartFile file);

    Plato createPlato(PlatoRequest request);

    /**
     * Actualiza los datos editables, categorías e ingredientes de un plato existente.
     */
    Plato updatePlato(Long platoId, PlatoRequest request);

    /**
     * Elimina un plato o lo pausa cuando los registros históricos aún lo referencian.
     *
     * @return true si se borró físicamente, false si quedó en PAUSADO
     */
    boolean deletePlato(Long platoId);

    /** Actualiza el descuento vigente de un plato tras validar ownership. */
    Plato actualizarDescuento(Long platoId, java.math.BigDecimal porcentaje);

    /** Quita el descuento vigente de un plato sin modificar su precio base. */
    Plato quitarDescuento(Long platoId);
}
