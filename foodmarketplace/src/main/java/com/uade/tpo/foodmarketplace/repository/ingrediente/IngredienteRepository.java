package com.uade.tpo.foodmarketplace.repository.ingrediente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

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

    Optional<Ingrediente> findByNombreNormalizado(String nombreNormalizado);

    /** Lectura bloqueante: ve el último valor confirmado aunque la transacción tenga un snapshot viejo. */
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select i from Ingrediente i where i.nombreNormalizado = :nombreNormalizado")
    Optional<Ingrediente> findByNombreNormalizadoForShare(@Param("nombreNormalizado") String nombreNormalizado);

    List<Ingrediente> findByNombreNormalizadoIsNull();

    /**
     * Inserta sin fallar si otro chef ya creó el mismo nombre: así la carrera no aborta la
     * transacción del plato y se vuelve a leer el existente.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = false)
    @Query(value = "insert into ingredientes (nombre, descripcion, nombre_normalizado) "
            + "values (:nombre, :descripcion, :nombreNormalizado) "
            + "on duplicate key update nombre_normalizado = nombre_normalizado", nativeQuery = true)
    int insertarSiNoExiste(@Param("nombre") String nombre, @Param("descripcion") String descripcion,
            @Param("nombreNormalizado") String nombreNormalizado);
}
