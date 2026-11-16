package com.uade.tpo.foodmarketplace.entity.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.uade.tpo.foodmarketplace.entity.order.EstadoPedido;

public record SubPedidoChefResponse(
        Long id,
        Long pedidoId,
        LocalDateTime fechaCreacion,
        String clienteNombre,
        Long chefId,
        String chefNombre,
        EstadoPedido estado,
        BigDecimal subtotal,
        List<DetallePedidoResponse> detalles) {
}
