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
import com.uade.tpo.foodmarketplace.entity.dto.common.ApiResponse;
import com.uade.tpo.foodmarketplace.entity.dto.order.EstadoSubPedidoRequest;
import com.uade.tpo.foodmarketplace.entity.dto.order.SubPedidoChefResponse;
import com.uade.tpo.foodmarketplace.service.order.SubPedidoChefService;
import com.uade.tpo.foodmarketplace.exceptions.order.SubPedidoNotFoundException;

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
    public ResponseEntity<ApiResponse<SubPedidoChefResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Subpedido obtenido correctamente",
                subPedidoChefService.getSubPedidoById(id).map(ResponseMapper::subPedido)
                        .orElseThrow(SubPedidoNotFoundException::new)));
    }

    /**
     * Devuelve todos los subpedidos generados para la orden solicitada.
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<SubPedidoChefResponse>>> getByOrder(@PathVariable Long orderId) {
        List<SubPedidoChefResponse> subPedidos = subPedidoChefService.getSubPedidosByOrderId(orderId).stream()
                .map(ResponseMapper::subPedido)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(subPedidos.isEmpty()
                ? "No hay subpedidos disponibles para este pedido"
                : "Subpedidos del pedido obtenidos correctamente", subPedidos.isEmpty() ? null : subPedidos));
    }

    /**
     * Devuelve todos los subpedidos actualmente asignados al chef solicitado.
     */
    @GetMapping("/chef/{chefId}")
    public ResponseEntity<ApiResponse<List<SubPedidoChefResponse>>> getByChef(@PathVariable Long chefId) {
        List<SubPedidoChefResponse> subPedidos = subPedidoChefService.getSubPedidosByChefId(chefId).stream()
                .map(ResponseMapper::subPedido)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(subPedidos.isEmpty()
                ? "No hay subpedidos disponibles para este chef"
                : "Subpedidos del chef obtenidos correctamente", subPedidos.isEmpty() ? null : subPedidos));
    }

    /**
     * Aplica una transición de estado validada a un subpedido.
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<SubPedidoChefResponse>> actualizarEstado(@PathVariable Long id,
            @Valid @RequestBody EstadoSubPedidoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Estado del subpedido actualizado correctamente",
                ResponseMapper.subPedido(subPedidoChefService.actualizarEstado(id, request.getEstado()))));
    }
}
