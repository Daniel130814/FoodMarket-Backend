package com.uade.tpo.foodmarketplace.repository.pago;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.tpo.foodmarketplace.entity.pago.Pago;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    boolean existsByPedidoIdAndEstado(Long pedidoId, com.uade.tpo.foodmarketplace.entity.pago.EstadoPago estado);
    long countByPedidoIdAndEstado(Long pedidoId, com.uade.tpo.foodmarketplace.entity.pago.EstadoPago estado);
}
