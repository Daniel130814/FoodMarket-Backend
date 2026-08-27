package com.uade.tpo.foodmarketplace.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.foodmarketplace.entity.Order;
import com.uade.tpo.foodmarketplace.entity.User;
import com.uade.tpo.foodmarketplace.exceptions.UserNotFoundException;
import com.uade.tpo.foodmarketplace.repository.OrderRepository;
import com.uade.tpo.foodmarketplace.repository.UserRepository;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<Order> getOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public Order createOrder(Float price, Long userId)
            throws UserNotFoundException {

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Order order = new Order();
        order.setPrice(price);
        order.setUser(user);

        return orderRepository.save(order);
    }
}
