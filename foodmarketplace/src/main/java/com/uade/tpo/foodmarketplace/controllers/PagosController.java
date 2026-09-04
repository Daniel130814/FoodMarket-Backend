package com.uade.tpo.foodmarketplace.controllers;

import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.foodmarketplace.entity.dto.EstadoPagoRequest;
import com.uade.tpo.foodmarketplace.entity.dto.PagoRequest;
import com.uade.tpo.foodmarketplace.entity.dto.PagoResponse;
import com.uade.tpo.foodmarketplace.entity.dto.ResponseMapper;
import com.uade.tpo.foodmarketplace.service.PagoService;

@RestController
@RequestMapping("pagos")
public class PagosController {

    @Autowired
    private PagoService pagoService;

    @GetMapping
    public ResponseEntity<List<PagoResponse>> getPagos() {
        return ResponseEntity.ok(pagoService.getPagos().stream().map(ResponseMapper::pago).toList());
    }

    @GetMapping("/{pagoId}")
    public ResponseEntity<PagoResponse> getPagoById(@PathVariable("pagoId") Long pagoId) {
        return pagoService.getPagoById(pagoId).map(ResponseMapper::pago).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("createPago")
    public ResponseEntity<PagoResponse> createPago(@Valid @RequestBody PagoRequest pagoRequest) {
        var result = pagoService.createPago(
                pagoRequest.getMedioPago(),
                pagoRequest.getPedidoId());

        return ResponseEntity
                .created(URI.create("/pagos/" + result.getId()))
                .body(ResponseMapper.pago(result));
    }

    @PatchMapping("/{pagoId}/estado")
    public ResponseEntity<PagoResponse> actualizarEstadoPago(@PathVariable("pagoId") Long pagoId,
            @Valid @RequestBody EstadoPagoRequest estadoPagoRequest) {
        return ResponseEntity.ok(ResponseMapper.pago(pagoService.actualizarEstadoPago(pagoId, estadoPagoRequest.getEstado())));
    }
}
