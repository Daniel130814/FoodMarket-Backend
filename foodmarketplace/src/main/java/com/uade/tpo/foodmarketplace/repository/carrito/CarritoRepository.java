package com.uade.tpo.foodmarketplace.repository.carrito;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uade.tpo.foodmarketplace.entity.carrito.Carrito;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    Optional<Carrito> findByClienteId(Long clienteId);

    /**
     * Serializa las mutaciones de los items de un mismo carrito.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Carrito c where c.cliente.id = :clienteId")
    Optional<Carrito> findByClienteIdForUpdate(@Param("clienteId") Long clienteId);
}
