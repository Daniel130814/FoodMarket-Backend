package com.uade.tpo.foodmarketplace.repository.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.tpo.foodmarketplace.entity.order.DetallePedido;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    List<DetallePedido> findBySubPedidoChefPedidoId(Long pedidoId);

    boolean existsBySubPedidoChefPedidoUserIdAndPlatoIdAndSubPedidoChefEstado(Long clienteId, Long platoId,
            com.uade.tpo.foodmarketplace.entity.order.EstadoPedido estado);

    /**
     * Indica si un plato forma parte de algún detalle histórico de orden.
     */
    boolean existsByPlatoId(Long platoId);
}
