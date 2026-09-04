package com.uade.tpo.foodmarketplace.repository.resena;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.tpo.foodmarketplace.entity.resena.Resena;

public interface ResenaRepository extends JpaRepository<Resena, Long> {

    List<Resena> findByPlatoId(Long platoId);

    boolean existsByClienteIdAndPlatoId(Long clienteId, Long platoId);

    /**
     * Indica si un plato tiene reseñas que deben conservarse como datos históricos.
     */
    boolean existsByPlatoId(Long platoId);

    @org.springframework.data.jpa.repository.Query("select avg(r.calificacion) from Resena r where r.plato.chef.id = :chefId")
    Double findPromedioCalificacionesByChefId(@org.springframework.data.repository.query.Param("chefId") Long chefId);
}
