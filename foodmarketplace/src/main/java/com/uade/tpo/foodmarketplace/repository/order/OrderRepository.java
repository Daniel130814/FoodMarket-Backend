package com.uade.tpo.foodmarketplace.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.tpo.foodmarketplace.entity.order.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Indicates whether a delivery address is referenced by an order.
     */
    boolean existsByDomicilioEntregaId(Long domicilioId);
}
