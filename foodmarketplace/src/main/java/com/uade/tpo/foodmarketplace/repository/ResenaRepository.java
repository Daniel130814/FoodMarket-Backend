package com.uade.tpo.foodmarketplace.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.tpo.foodmarketplace.entity.Resena;

public interface ResenaRepository extends JpaRepository<Resena, Long> {

    List<Resena> findByPlatoId(Long platoId);

    boolean existsByClienteIdAndPlatoId(Long clienteId, Long platoId);
}
