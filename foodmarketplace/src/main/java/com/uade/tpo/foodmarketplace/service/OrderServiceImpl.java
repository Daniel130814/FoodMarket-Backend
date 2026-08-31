package com.uade.tpo.foodmarketplace.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.foodmarketplace.entity.Domicilio;
import com.uade.tpo.foodmarketplace.entity.EstadoPedido;
import com.uade.tpo.foodmarketplace.entity.Order;
import com.uade.tpo.foodmarketplace.entity.User;
import com.uade.tpo.foodmarketplace.exceptions.DomicilioNoPerteneceAlUsuarioException;
import com.uade.tpo.foodmarketplace.exceptions.DomicilioNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.UserNotFoundException;
import com.uade.tpo.foodmarketplace.repository.DomicilioRepository;
import com.uade.tpo.foodmarketplace.repository.OrderRepository;
import com.uade.tpo.foodmarketplace.repository.UserRepository;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DomicilioRepository domicilioRepository;

    @Override
    public List<Order> getOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public Order createOrder(Float precioFinal, Long userId, Long domicilioEntregaId)
            throws UserNotFoundException {

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Domicilio domicilioEntrega = domicilioRepository.findById(domicilioEntregaId)
                .orElseThrow(DomicilioNotFoundException::new);

        if (!domicilioEntrega.getUsuario().getId().equals(userId)) {
            throw new DomicilioNoPerteneceAlUsuarioException();
        }

        Order order = new Order();
        order.setPrecioFinal(precioFinal);
        order.setFechaCreacion(LocalDateTime.now());
        order.setEstado(EstadoPedido.PENDIENTE);
        order.setUser(user);
        order.setDomicilioEntrega(domicilioEntrega);

    
        return orderRepository.save(order);
    }
}
