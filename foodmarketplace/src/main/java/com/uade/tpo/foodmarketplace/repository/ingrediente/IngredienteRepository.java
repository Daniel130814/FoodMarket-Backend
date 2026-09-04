package com.uade.tpo.foodmarketplace.repository.ingrediente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uade.tpo.foodmarketplace.entity.ingrediente.Ingrediente;

public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {

    /**
     * Checks whether an ingredient name is already registered, ignoring case.
     */
    boolean existsByNombreIgnoreCase(String nombre);

    /**
     * Checks whether another ingredient already uses the supplied name.
     */
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    /**
     * Indicates whether the ingredient participates in at least one dish recipe.
     */
    @Query("select case when count(pi) > 0 then true else false end "
            + "from PlatoIngrediente pi where pi.ingrediente.id = :ingredienteId")
    boolean existsUsedInPlatos(@Param("ingredienteId") Long ingredienteId);
}
