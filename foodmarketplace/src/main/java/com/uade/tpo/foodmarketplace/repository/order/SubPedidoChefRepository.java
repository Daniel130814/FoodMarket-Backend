package com.uade.tpo.foodmarketplace.repository.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.tpo.foodmarketplace.entity.order.SubPedidoChef;

public interface SubPedidoChefRepository extends JpaRepository<SubPedidoChef, Long> {

    /**
     * Obtiene los subpedidos independientes generados para una orden.
     */
    List<SubPedidoChef> findByPedidoId(Long pedidoId);

    /**
     * Obtiene los subpedidos asignados a un chef específico.
     */
    List<SubPedidoChef> findByChefId(Long chefId);
}
