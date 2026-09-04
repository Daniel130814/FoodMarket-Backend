package com.uade.tpo.foodmarketplace.entity.dto.order;

import java.math.BigDecimal;
import java.util.List;

import com.uade.tpo.foodmarketplace.entity.order.EstadoPedido;

public record SubPedidoChefResponse(
        Long id,
        Long chefId,
        EstadoPedido estado,
        BigDecimal subtotal,
        List<DetallePedidoResponse> detalles) {
}
