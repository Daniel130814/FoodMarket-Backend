package com.uade.tpo.foodmarketplace.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.tpo.foodmarketplace.entity.DetallePedido;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    List<DetallePedido> findBySubPedidoChefPedidoId(Long pedidoId);

    boolean existsBySubPedidoChefPedidoUserIdAndPlatoIdAndSubPedidoChefEstado(Long clienteId, Long platoId,
            com.uade.tpo.foodmarketplace.entity.EstadoPedido estado);
}
