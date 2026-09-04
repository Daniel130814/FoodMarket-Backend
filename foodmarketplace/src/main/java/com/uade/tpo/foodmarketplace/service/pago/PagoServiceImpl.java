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
import com.uade.tpo.foodmarketplace.entity.pago.Pago;
import com.uade.tpo.foodmarketplace.exceptions.pago.PagoDuplicateException;
import com.uade.tpo.foodmarketplace.exceptions.pago.PagoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.order.PedidoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.common.BusinessRuleException;
import com.uade.tpo.foodmarketplace.repository.order.OrderRepository;
import com.uade.tpo.foodmarketplace.repository.pago.PagoRepository;

@Service
public class PagoServiceImpl implements PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public List<Pago> getPagos() {
        return pagoRepository.findAll();
    }

    @Override
    public Optional<Pago> getPagoById(Long pagoId) {
        return pagoRepository.findById(pagoId);
    }

    @Override
    @Transactional
    public Pago createPago(MedioPago medioPago, Long pedidoId) {
        Order pedido = orderRepository.findById(pedidoId)
                .orElseThrow(PedidoNotFoundException::new);

        if (pedido.isPagoBloqueado() || pagoRepository.countByPedidoIdAndEstado(pedidoId, EstadoPago.RECHAZADO) >= 5) {
            pedido.setPagoBloqueado(true);
            throw new BusinessRuleException("Los intentos de pago para esta orden estan bloqueados");
        }
        if (pagoRepository.existsByPedidoIdAndEstado(pedidoId, EstadoPago.APROBADO)) {
            throw new BusinessRuleException("La orden ya posee un pago aprobado");
        }

        Pago pago = new Pago();
        pago.setMonto(pedido.getPrecioFinal());
        pago.setFechaCreacion(LocalDateTime.now());
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setMedioPago(medioPago);
        pago.setPedido(pedido);

        return pagoRepository.save(pago);
    }

    @Override
    @Transactional
    public Pago actualizarEstadoPago(Long pagoId, EstadoPago estado) {
        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(PagoNotFoundException::new);

        if (estado == EstadoPago.APROBADO && pagoRepository.existsByPedidoIdAndEstado(pago.getPedido().getId(), EstadoPago.APROBADO)
                && pago.getEstado() != EstadoPago.APROBADO) {
            throw new BusinessRuleException("La orden ya posee un pago aprobado");
        }
        if (estado == EstadoPago.RECHAZADO && pago.getEstado() != EstadoPago.RECHAZADO) {
            long rechazados = pagoRepository.countByPedidoIdAndEstado(pago.getPedido().getId(), EstadoPago.RECHAZADO);
            if (rechazados >= 5) throw new BusinessRuleException("Se alcanzo el maximo de intentos rechazados");
            if (rechazados + 1 == 5) pago.getPedido().setPagoBloqueado(true);
        }
        pago.setEstado(estado);

        if (estado == EstadoPago.APROBADO && pago.getFechaPago() == null) {
            pago.setFechaPago(LocalDateTime.now());
        }

        return pagoRepository.save(pago);
    }
}
