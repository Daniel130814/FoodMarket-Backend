package com.uade.tpo.foodmarketplace.service.order;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.foodmarketplace.entity.order.Order;
import com.uade.tpo.foodmarketplace.entity.dto.order.OrderRequest;

public interface OrderService {

    List<Order> getOrders();

    Optional<Order> getOrderById(Long orderId);

    Order createOrder(OrderRequest request);

    Order cancelarOrder(Long orderId);

    /**
     * Deriva y persiste el estado general de la orden a partir de sus subpedidos de chef.
     */
    Order recalcularEstadoDesdeSubPedidos(Order order);
}
