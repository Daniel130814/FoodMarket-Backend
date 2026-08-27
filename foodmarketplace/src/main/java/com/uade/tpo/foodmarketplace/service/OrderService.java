package com.uade.tpo.foodmarketplace.service;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.foodmarketplace.entity.Order;

public interface OrderService {

    List<Order> getOrders();

    Optional<Order> getOrderById(Long orderId);

    Order createOrder(Float price, Long userId);
}
