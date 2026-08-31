package com.uade.tpo.foodmarketplace.service;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.foodmarketplace.entity.DetallePedido;

public interface DetallePedidoService {

    List<DetallePedido> getDetallesPedido();

    Optional<DetallePedido> getDetallePedidoById(Long detallePedidoId);

    List<DetallePedido> getDetallesPedidoByPedidoId(Long pedidoId);

    DetallePedido createDetallePedido(Integer cantidad, Long pedidoId, Long menuSemanalId);
}
