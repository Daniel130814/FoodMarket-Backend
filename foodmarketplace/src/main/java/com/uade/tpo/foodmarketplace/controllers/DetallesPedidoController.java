package com.uade.tpo.foodmarketplace.controllers;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.foodmarketplace.entity.DetallePedido;
import com.uade.tpo.foodmarketplace.entity.dto.DetallePedidoRequest;
import com.uade.tpo.foodmarketplace.service.DetallePedidoService;

@RestController
@RequestMapping("detallesPedido")
public class DetallesPedidoController {

    @Autowired
    private DetallePedidoService detallePedidoService;

    @GetMapping
    public ResponseEntity<List<DetallePedido>> getDetallesPedido() {
        return ResponseEntity.ok(detallePedidoService.getDetallesPedido());
    }

    @GetMapping("/{detallePedidoId}")
    public ResponseEntity<DetallePedido> getDetallePedidoById(
            @PathVariable("detallePedidoId") Long detallePedidoId) {
        Optional<DetallePedido> detallePedido = detallePedidoService.getDetallePedidoById(detallePedidoId);

        if (detallePedido.isPresent()) {
            return ResponseEntity.ok(detallePedido.get());
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<List<DetallePedido>> getDetallesPedidoByPedidoId(
            @PathVariable("pedidoId") Long pedidoId) {
        return ResponseEntity.ok(detallePedidoService.getDetallesPedidoByPedidoId(pedidoId));
    }

    @PostMapping("createDetallePedido")
    public ResponseEntity<DetallePedido> createDetallePedido(
            @RequestBody DetallePedidoRequest detallePedidoRequest) {
        DetallePedido result = detallePedidoService.createDetallePedido(
                detallePedidoRequest.getCantidad(),
                detallePedidoRequest.getPedidoId(),
                detallePedidoRequest.getMenuSemanalId());

        return ResponseEntity
                .created(URI.create("/detallesPedido/" + result.getId()))
                .body(result);
    }
}
