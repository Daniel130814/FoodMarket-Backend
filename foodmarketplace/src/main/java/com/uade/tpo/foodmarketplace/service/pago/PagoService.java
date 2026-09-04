package com.uade.tpo.foodmarketplace.service.pago;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.foodmarketplace.entity.pago.EstadoPago;
import com.uade.tpo.foodmarketplace.entity.pago.MedioPago;
import com.uade.tpo.foodmarketplace.entity.pago.Pago;

public interface PagoService {

    List<Pago> getPagos();

    Optional<Pago> getPagoById(Long pagoId);

    Pago createPago(MedioPago medioPago, Long pedidoId);

    Pago actualizarEstadoPago(Long pagoId, EstadoPago estado);
}
