package com.uade.tpo.foodmarketplace.repository.carrito;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import com.uade.tpo.foodmarketplace.entity.carrito.ItemCarrito;

public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {

    Optional<ItemCarrito> findByCarritoIdAndPlatoId(Long carritoId, Long platoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from ItemCarrito i where i.carrito.id = :carritoId and i.plato.id = :platoId")
    Optional<ItemCarrito> findByCarritoIdAndPlatoIdForUpdate(@Param("carritoId") Long carritoId,
            @Param("platoId") Long platoId);

    /** Indica si un plato continúa referenciado por algún carrito activo. */
    boolean existsByPlatoId(Long platoId);
}
