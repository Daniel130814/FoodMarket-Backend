package com.uade.tpo.foodmarketplace.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.tpo.foodmarketplace.entity.Plato;

public interface PlatoRepository extends JpaRepository<Plato, Long> {

    List<Plato> findByMenuSemanalId(Long menuSemanalId);
}
