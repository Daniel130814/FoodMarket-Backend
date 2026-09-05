package com.uade.tpo.foodmarketplace.repository.plato;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import com.uade.tpo.foodmarketplace.entity.plato.Plato;
import com.uade.tpo.foodmarketplace.entity.plato.EstadoPlato;

public interface PlatoRepository extends JpaRepository<Plato, Long> {

    List<Plato> findByEstado(EstadoPlato estado);

    Optional<Plato> findByIdAndEstado(Long id, EstadoPlato estado);

    /**
     * Indica si una categoría está asignada a al menos un plato.
     */
    boolean existsByCategoriasId(Long categoryId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from Plato p where p.id = :id")
    java.util.Optional<Plato> findByIdForUpdate(
            @org.springframework.data.repository.query.Param("id") Long id
    );
}