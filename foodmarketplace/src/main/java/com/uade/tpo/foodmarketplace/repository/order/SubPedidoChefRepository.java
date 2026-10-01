package com.uade.tpo.foodmarketplace.repository.order;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uade.tpo.foodmarketplace.entity.order.SubPedidoChef;

public interface SubPedidoChefRepository extends JpaRepository<SubPedidoChef, Long> {

    /**
     * Obtiene los subpedidos independientes generados para una orden.
     */
    List<SubPedidoChef> findByPedidoId(Long pedidoId);

    List<SubPedidoChef> findByPedidoIdAndChefId(Long pedidoId, Long chefId);

    /**
     * Obtiene los subpedidos asignados a un chef específico.
     */
    List<SubPedidoChef> findByChefId(Long chefId);

    /** Lee el pedido padre antes de adquirir su lock, sin gestionar una entidad potencialmente obsoleta. */
    @Query("select s.pedido.id from SubPedidoChef s where s.id = :subPedidoId")
    Optional<Long> findPedidoIdById(@Param("subPedidoId") Long subPedidoId);

    /** Se bloquea después de Order para conservar un orden de locks uniforme. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SubPedidoChef s where s.id = :subPedidoId")
    Optional<SubPedidoChef> findByIdForUpdate(@Param("subPedidoId") Long subPedidoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SubPedidoChef s where s.pedido.id = :pedidoId")
    List<SubPedidoChef> findByPedidoIdForUpdate(@Param("pedidoId") Long pedidoId);
}
