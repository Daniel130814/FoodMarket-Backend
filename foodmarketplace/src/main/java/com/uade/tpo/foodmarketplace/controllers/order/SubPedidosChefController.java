package com.uade.tpo.foodmarketplace.controllers.order;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.foodmarketplace.entity.dto.common.ResponseMapper;
import com.uade.tpo.foodmarketplace.entity.dto.order.EstadoSubPedidoRequest;
import com.uade.tpo.foodmarketplace.entity.dto.order.SubPedidoChefResponse;
import com.uade.tpo.foodmarketplace.service.order.SubPedidoChefService;

import jakarta.validation.Valid;

/**
 * Expone las operaciones del ciclo de vida de los subpedidos individuales para chefs.
 */
@RestController
@RequestMapping("subpedidos")
public class SubPedidosChefController {

    private final SubPedidoChefService subPedidoChefService;

    public SubPedidosChefController(SubPedidoChefService subPedidoChefService) {
        this.subPedidoChefService = subPedidoChefService;
    }

    /**
     * Devuelve un subpedido como DTO de respuesta sin referencias circulares de JPA.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SubPedidoChefResponse> getById(@PathVariable Long id) {
        return subPedidoChefService.getSubPedidoById(id)
                .map(ResponseMapper::subPedido)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Devuelve todos los subpedidos generados para la orden solicitada.
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<SubPedidoChefResponse>> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(subPedidoChefService.getSubPedidosByOrderId(orderId).stream()
                .map(ResponseMapper::subPedido)
                .toList());
    }

    /**
     * Devuelve todos los subpedidos actualmente asignados al chef solicitado.
     */
    @GetMapping("/chef/{chefId}")
    public ResponseEntity<List<SubPedidoChefResponse>> getByChef(@PathVariable Long chefId) {
        return ResponseEntity.ok(subPedidoChefService.getSubPedidosByChefId(chefId).stream()
                .map(ResponseMapper::subPedido)
                .toList());
    }

    /**
     * Aplica una transición de estado validada a un subpedido.
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<SubPedidoChefResponse> actualizarEstado(@PathVariable Long id,
            @Valid @RequestBody EstadoSubPedidoRequest request) {
        return ResponseEntity.ok(ResponseMapper.subPedido(
                subPedidoChefService.actualizarEstado(id, request.getEstado())));
    }
}
