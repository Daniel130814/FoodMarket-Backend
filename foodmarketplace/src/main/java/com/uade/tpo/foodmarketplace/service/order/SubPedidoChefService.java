package com.uade.tpo.foodmarketplace.service.order;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.foodmarketplace.entity.order.EstadoPedido;
import com.uade.tpo.foodmarketplace.entity.order.SubPedidoChef;

/**
 * Define las operaciones del ciclo de vida independiente de cada subpedido de chef.
 */
public interface SubPedidoChefService {

    /**
     * Busca un subpedido por su identificador.
     */
    Optional<SubPedidoChef> getSubPedidoById(Long subPedidoId);

    /**
     * Lista los subpedidos que pertenecen a una orden.
     */
    List<SubPedidoChef> getSubPedidosByOrderId(Long orderId);

    /**
     * Lista los subpedidos asignados a un chef.
     */
    List<SubPedidoChef> getSubPedidosByChefId(Long chefId);

    /**
     * Cambia el estado de un subpedido tras validar la secuencia permitida.
     */
    SubPedidoChef actualizarEstado(Long subPedidoId, EstadoPedido nuevoEstado);
}
