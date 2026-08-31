package com.uade.tpo.foodmarketplace.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.foodmarketplace.entity.EstadoPago;
import com.uade.tpo.foodmarketplace.entity.MedioPago;
import com.uade.tpo.foodmarketplace.entity.Order;
import com.uade.tpo.foodmarketplace.entity.Pago;
import com.uade.tpo.foodmarketplace.exceptions.PagoDuplicateException;
import com.uade.tpo.foodmarketplace.exceptions.PagoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.PedidoNotFoundException;
import com.uade.tpo.foodmarketplace.repository.OrderRepository;
import com.uade.tpo.foodmarketplace.repository.PagoRepository;

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
    public Pago createPago(MedioPago medioPago, Long pedidoId) {
        Order pedido = orderRepository.findById(pedidoId)
                .orElseThrow(PedidoNotFoundException::new);

        if (pagoRepository.existsByPedidoId(pedidoId)) {
            throw new PagoDuplicateException();
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
    public Pago actualizarEstadoPago(Long pagoId, EstadoPago estado) {
        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(PagoNotFoundException::new);

        pago.setEstado(estado);

        if (estado == EstadoPago.APROBADO && pago.getFechaPago() == null) {
            pago.setFechaPago(LocalDateTime.now());
        }

        return pagoRepository.save(pago);
    }
}
