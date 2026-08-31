package com.uade.tpo.foodmarketplace.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.tpo.foodmarketplace.entity.Domicilio;

public interface DomicilioRepository extends JpaRepository<Domicilio, Long> {

    List<Domicilio> findByUsuarioId(Long usuarioId);
}
