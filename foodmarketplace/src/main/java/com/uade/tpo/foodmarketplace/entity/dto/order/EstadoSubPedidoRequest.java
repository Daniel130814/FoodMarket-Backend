package com.uade.tpo.foodmarketplace.entity.dto.order;

import com.uade.tpo.foodmarketplace.entity.order.EstadoPedido;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Recibe únicamente el estado destino de un subpedido, nunca la entidad JPA completa.
 */
@Data
public class EstadoSubPedidoRequest {

    @NotNull
    private EstadoPedido estado;
}
