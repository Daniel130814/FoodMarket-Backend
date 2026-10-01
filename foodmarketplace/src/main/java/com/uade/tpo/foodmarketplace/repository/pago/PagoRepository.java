package com.uade.tpo.foodmarketplace.repository.pago;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uade.tpo.foodmarketplace.entity.pago.Pago;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByPedidoUserId(Long userId);

    boolean existsByPedidoIdAndEstado(Long pedidoId, com.uade.tpo.foodmarketplace.entity.pago.EstadoPago estado);
    long countByPedidoIdAndEstado(Long pedidoId, com.uade.tpo.foodmarketplace.entity.pago.EstadoPago estado);

    /** Obtiene sólo el identificador necesario antes de serializar el agregado Order. */
    @Query("select p.pedido.id from Pago p where p.id = :pagoId")
    Optional<Long> findPedidoIdById(@Param("pagoId") Long pagoId);

    /** Debe usarse después del lock de Order para no trabajar con un Pago obsoleto. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pago p where p.id = :pagoId")
    Optional<Pago> findByIdForUpdate(@Param("pagoId") Long pagoId);

    /** Current read de todos los pagos luego de bloquear su Order. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pago p where p.pedido.id = :pedidoId")
    List<Pago> findByPedidoIdForUpdate(@Param("pedidoId") Long pedidoId);
}
