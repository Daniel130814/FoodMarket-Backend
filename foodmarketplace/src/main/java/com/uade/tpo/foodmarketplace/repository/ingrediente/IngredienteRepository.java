package com.uade.tpo.foodmarketplace.repository.ingrediente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uade.tpo.foodmarketplace.entity.ingrediente.Ingrediente;

public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {

    /**
     * Comprueba si un nombre de ingrediente ya está registrado, sin distinguir mayúsculas.
     */
    boolean existsByNombreIgnoreCase(String nombre);

    /**
     * Comprueba si otro ingrediente ya utiliza el nombre indicado.
     */
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    /**
     * Indica si el ingrediente participa en al menos una receta de plato.
     */
    @Query("select case when count(pi) > 0 then true else false end "
            + "from PlatoIngrediente pi where pi.ingrediente.id = :ingredienteId")
    boolean existsUsedInPlatos(@Param("ingredienteId") Long ingredienteId);
}
