package com.uade.tpo.foodmarketplace.controllers.order;

import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.foodmarketplace.entity.dto.order.OrderRequest;
import com.uade.tpo.foodmarketplace.entity.dto.order.OrderResponse;
import com.uade.tpo.foodmarketplace.entity.dto.common.ResponseMapper;
import com.uade.tpo.foodmarketplace.service.order.OrderService;

@RestController
@RequestMapping("orders")
public class OrdersController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders() {
        return ResponseEntity.ok(orderService.getOrders().stream().map(ResponseMapper::order).toList());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable("orderId") Long orderId) {
        return orderService.getOrderById(orderId).map(ResponseMapper::order).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("createOrder")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        var result = orderService.createOrder(orderRequest);

        return ResponseEntity
                .created(URI.create("/orders/" + result.getId()))
                .body(ResponseMapper.order(result));
    }

    @PatchMapping("/{orderId}/cancelar")
    public ResponseEntity<OrderResponse> cancelarOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ResponseMapper.order(orderService.cancelarOrder(orderId)));
    }
}
