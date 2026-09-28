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
import com.uade.tpo.foodmarketplace.entity.dto.common.ApiResponse;
import com.uade.tpo.foodmarketplace.exceptions.order.PedidoNotFoundException;
import com.uade.tpo.foodmarketplace.service.order.OrderService;

@RestController
@RequestMapping("orders")
public class OrdersController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrders() {
        List<OrderResponse> data = orderService.getOrders().stream().map(ResponseMapper::order).toList();
        return ResponseEntity.ok(ApiResponse.ok(data.isEmpty() ? "No hay pedidos registrados para mostrar." : "Pedidos obtenidos correctamente", data.isEmpty() ? null : data));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok("Pedido obtenido correctamente", orderService.getOrderById(orderId).map(ResponseMapper::order).orElseThrow(PedidoNotFoundException::new)));
    }

    @PostMapping("createOrder")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        var result = orderService.createOrder(orderRequest);

        return ResponseEntity
                .created(URI.create("/orders/" + result.getId()))
                .body(ApiResponse.ok("Pedido creado correctamente", ResponseMapper.order(result)));
    }

    @PatchMapping("/{orderId}/cancelar")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelarOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok("Pedido cancelado correctamente", ResponseMapper.order(orderService.cancelarOrder(orderId))));
    }
}
