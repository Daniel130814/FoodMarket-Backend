package com.uade.tpo.foodmarketplace.repository.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.tpo.foodmarketplace.entity.order.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    /**
     * Indica si un domicilio de entrega está referenciado por una orden.
     */
    boolean existsByDomicilioEntregaId(Long domicilioId);
}
