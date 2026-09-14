package com.uade.tpo.foodmarketplace.repository.order;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uade.tpo.foodmarketplace.entity.order.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    /**
     * Serializa los comandos que cambian invariantes del agregado Order.
     * Los GET normales continúan utilizando findById sin bloqueo.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);

    /**
     * Indica si un domicilio de entrega está referenciado por una orden.
     */
    boolean existsByDomicilioEntregaId(Long domicilioId);
}
