package com.uade.tpo.foodmarketplace.controllers;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.foodmarketplace.entity.Pago;
import com.uade.tpo.foodmarketplace.entity.dto.EstadoPagoRequest;
import com.uade.tpo.foodmarketplace.entity.dto.PagoRequest;
import com.uade.tpo.foodmarketplace.service.PagoService;

@RestController
@RequestMapping("pagos")
public class PagosController {

    @Autowired
    private PagoService pagoService;

    @GetMapping
    public ResponseEntity<List<Pago>> getPagos() {
        return ResponseEntity.ok(pagoService.getPagos());
    }

    @GetMapping("/{pagoId}")
    public ResponseEntity<Pago> getPagoById(@PathVariable("pagoId") Long pagoId) {
        Optional<Pago> pago = pagoService.getPagoById(pagoId);

        if (pago.isPresent()) {
            return ResponseEntity.ok(pago.get());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("createPago")
    public ResponseEntity<Pago> createPago(@RequestBody PagoRequest pagoRequest) {
        Pago result = pagoService.createPago(
                pagoRequest.getMedioPago(),
                pagoRequest.getPedidoId());

        return ResponseEntity
                .created(URI.create("/pagos/" + result.getId()))
                .body(result);
    }

    @PatchMapping("/{pagoId}/estado")
    public ResponseEntity<Pago> actualizarEstadoPago(@PathVariable("pagoId") Long pagoId,
            @RequestBody EstadoPagoRequest estadoPagoRequest) {
        return ResponseEntity.ok(pagoService.actualizarEstadoPago(
                pagoId,
                estadoPagoRequest.getEstado()));
    }
}
