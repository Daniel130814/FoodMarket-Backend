package com.uade.tpo.foodmarketplace.entity.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.uade.tpo.foodmarketplace.entity.order.EstadoPedido;

public record OrderResponse(
        Long id,
        Long userId,
        Long domicilioEntregaId,
        LocalDateTime fechaCreacion,
        EstadoPedido estado,
        BigDecimal precioFinal,
        boolean pagoBloqueado,
        List<SubPedidoChefResponse> subPedidos) {
}
