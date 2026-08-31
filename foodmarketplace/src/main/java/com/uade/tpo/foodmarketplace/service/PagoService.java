package com.uade.tpo.foodmarketplace.service;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.foodmarketplace.entity.EstadoPago;
import com.uade.tpo.foodmarketplace.entity.MedioPago;
import com.uade.tpo.foodmarketplace.entity.Pago;

public interface PagoService {

    List<Pago> getPagos();

    Optional<Pago> getPagoById(Long pagoId);

    Pago createPago(MedioPago medioPago, Long pedidoId);

    Pago actualizarEstadoPago(Long pagoId, EstadoPago estado);
}
