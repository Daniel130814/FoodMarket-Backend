package com.uade.tpo.foodmarketplace.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import com.uade.tpo.foodmarketplace.entity.Plato;

public interface PlatoRepository extends JpaRepository<Plato, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from Plato p where p.id = :id")
    java.util.Optional<Plato> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);
}
