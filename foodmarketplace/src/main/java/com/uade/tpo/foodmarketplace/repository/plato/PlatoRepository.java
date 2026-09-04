package com.uade.tpo.foodmarketplace.repository.plato;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import com.uade.tpo.foodmarketplace.entity.plato.Plato;

public interface PlatoRepository extends JpaRepository<Plato, Long> {

    /**
     * Indicates whether a category is assigned to at least one dish.
     */
    boolean existsByCategoriasId(Long categoryId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from Plato p where p.id = :id")
    java.util.Optional<Plato> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);
}
