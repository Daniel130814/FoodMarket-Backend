package com.uade.tpo.foodmarketplace.service.pago;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.foodmarketplace.entity.pago.EstadoPago;
import com.uade.tpo.foodmarketplace.entity.pago.MedioPago;
import com.uade.tpo.foodmarketplace.entity.order.Order;
import com.uade.tpo.foodmarketplace.entity.order.EstadoPedido;
import com.uade.tpo.foodmarketplace.entity.pago.Pago;
import com.uade.tpo.foodmarketplace.exceptions.pago.PagoDuplicateException;
import com.uade.tpo.foodmarketplace.exceptions.pago.PagoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.pago.InvalidPagoStateException;
import com.uade.tpo.foodmarketplace.exceptions.order.PedidoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.order.OrderCancelledException;
import com.uade.tpo.foodmarketplace.exceptions.common.BusinessRuleException;
import com.uade.tpo.foodmarketplace.repository.order.OrderRepository;
import com.uade.tpo.foodmarketplace.repository.pago.PagoRepository;
import com.uade.tpo.foodmarketplace.service.order.OrderService;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.security.AuthenticatedUserService;

@Service
public class PagoServiceImpl implements PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @Override
    public List<Pago> getPagos() {
        User currentUser = authenticatedUserService.getCurrentUser();
        return authenticatedUserService.isAdmin(currentUser)
                ? pagoRepository.findAll()
                : pagoRepository.findByPedidoUserId(currentUser.getId());
    }

    @Override
    public Optional<Pago> getPagoById(Long pagoId) {
        User currentUser = authenticatedUserService.getCurrentUser();
        Optional<Pago> pago = pagoRepository.findById(pagoId);
        pago.ifPresent(value -> authenticatedUserService.requireOwnerOrAdmin(
                currentUser, value.getPedido().getUser().getId()));
        return pago;
    }

    /**
     * Crea un nuevo intento de pago pendiente salvo que la orden esté cancelada o bloqueada para pagos.
     */
    @Override
    @Transactional
    public Pago createPago(MedioPago medioPago, Long pedidoId) {
        Order pedido = orderRepository.findById(pedidoId)
                .orElseThrow(PedidoNotFoundException::new);
        authenticatedUserService.requireOwnerOrAdmin(authenticatedUserService.getCurrentUser(),
                pedido.getUser().getId());

        // Una orden cancelada es terminal y no puede recibir nuevos intentos de pago.
        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new OrderCancelledException();
        }
        if (pedido.isPagoBloqueado() || pagoRepository.countByPedidoIdAndEstado(pedidoId, EstadoPago.RECHAZADO) >= 5) {
            pedido.setPagoBloqueado(true);
            throw new BusinessRuleException("Los intentos de pago para esta orden estan bloqueados");
        }
        if (pagoRepository.existsByPedidoIdAndEstado(pedidoId, EstadoPago.APROBADO)) {
            throw new InvalidPagoStateException("La orden ya posee un pago aprobado");
        }

        Pago pago = new Pago();
        pago.setMonto(pedido.getPrecioFinal());
        pago.setFechaCreacion(LocalDateTime.now());
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setMedioPago(medioPago);
        pago.setPedido(pedido);

        return pagoRepository.save(pago);
    }

    /**
     * Aplica una transición de pago válida y confirma la orden atómicamente cuando se aprueba.
     */
    @Override
    @Transactional
    public Pago actualizarEstadoPago(Long pagoId, EstadoPago estado) {
        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(PagoNotFoundException::new);
        Order pedido = pago.getPedido();

        // Se valida antes de cambiar datos para que un intento rechazado o reembolsado nunca se reabra.
        validarTransicion(pago.getEstado(), estado);
        if (estado == EstadoPago.APROBADO && pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new OrderCancelledException();
        }
        if (estado == EstadoPago.APROBADO && pagoRepository.existsByPedidoIdAndEstado(pedido.getId(), EstadoPago.APROBADO)
                && pago.getEstado() != EstadoPago.APROBADO) {
            throw new InvalidPagoStateException("La orden ya posee un pago aprobado");
        }
        if (estado == EstadoPago.RECHAZADO && pago.getEstado() != EstadoPago.RECHAZADO) {
            long rechazados = pagoRepository.countByPedidoIdAndEstado(pedido.getId(), EstadoPago.RECHAZADO);
            if (rechazados >= 5) {
                throw new BusinessRuleException("Se alcanzo el maximo de intentos rechazados");
            }
            if (rechazados + 1 == 5) {
                pedido.setPagoBloqueado(true);
            }
        }
        pago.setEstado(estado);

        if (estado == EstadoPago.APROBADO) {
            if (pago.getFechaPago() == null) {
                pago.setFechaPago(LocalDateTime.now());
            }
            confirmarOrderYSubPedidosPendientes(pedido);
        }

        return pagoRepository.save(pago);
    }

    /**
     * Confirma una orden pendiente y sus subpedidos pendientes después de su primer pago aprobado.
     */
    private void confirmarOrderYSubPedidosPendientes(Order pedido) {
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            return;
        }

        // Cada chef comienza en CONFIRMADO al mismo tiempo una vez que la compra fue pagada.
        pedido.getSubPedidos().stream()
                .filter(subPedido -> subPedido.getEstado() == EstadoPedido.PENDIENTE)
                .forEach(subPedido -> subPedido.setEstado(EstadoPedido.CONFIRMADO));
        orderService.recalcularEstadoDesdeSubPedidos(pedido);
    }

    /**
     * Valida el flujo finito de pagos: pendiente a aprobado/rechazado y luego aprobado a reembolsado.
     */
    private void validarTransicion(EstadoPago estadoActual, EstadoPago nuevoEstado) {
        boolean esValida = switch (estadoActual) {
            case PENDIENTE -> nuevoEstado == EstadoPago.APROBADO || nuevoEstado == EstadoPago.RECHAZADO;
            case APROBADO -> nuevoEstado == EstadoPago.REEMBOLSADO;
            case RECHAZADO, REEMBOLSADO -> false;
        };

        if (!esValida) {
            throw new InvalidPagoStateException(
                    "Transicion invalida de " + estadoActual + " a " + nuevoEstado + " para el pago");
        }
    }
}
