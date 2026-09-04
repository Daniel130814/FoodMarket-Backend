package com.uade.tpo.foodmarketplace.repository.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.tpo.foodmarketplace.entity.order.DetallePedido;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    List<DetallePedido> findBySubPedidoChefPedidoId(Long pedidoId);

    boolean existsBySubPedidoChefPedidoUserIdAndPlatoIdAndSubPedidoChefEstado(Long clienteId, Long platoId,
            com.uade.tpo.foodmarketplace.entity.order.EstadoPedido estado);

    /**
     * Indicates whether a dish is part of any historical order detail.
     */
    boolean existsByPlatoId(Long platoId);
}
