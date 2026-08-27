package com.uade.tpo.foodmarketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.tpo.foodmarketplace.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
