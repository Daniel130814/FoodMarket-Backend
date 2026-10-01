package com.uade.tpo.foodmarketplace.controllers.carrito;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.foodmarketplace.entity.dto.carrito.AddItemCarritoRequest;
import com.uade.tpo.foodmarketplace.entity.dto.carrito.CarritoResponse;
import com.uade.tpo.foodmarketplace.entity.dto.carrito.CheckoutCarritoRequest;
import com.uade.tpo.foodmarketplace.entity.dto.carrito.UpdateItemCarritoRequest;
import com.uade.tpo.foodmarketplace.entity.dto.common.ApiResponse;
import com.uade.tpo.foodmarketplace.entity.dto.order.OrderResponse;
import com.uade.tpo.foodmarketplace.service.carrito.CarritoService;

import jakarta.validation.Valid;

/** Expone exclusivamente el carrito asociado al usuario CLIENTE presente en el JWT. */
@RestController
@RequestMapping("carrito")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    /** Devuelve o crea de forma lazy el carrito del cliente autenticado. */
    @GetMapping
    public ResponseEntity<ApiResponse<CarritoResponse>> getMiCarrito() {
        CarritoResponse carrito = carritoService.getMiCarrito();
        String message = carrito.items().isEmpty() ? "El carrito está vacío" : "Carrito obtenido correctamente";
        return ResponseEntity.ok(ApiResponse.ok(message, carrito));
    }

    /** Agrega un plato sin reservar stock. */
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CarritoResponse>> agregarItem(@Valid @RequestBody AddItemCarritoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Item agregado correctamente", carritoService.agregarItem(request)));
    }

    /** Cambia la cantidad de un item del carrito del cliente autenticado. */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CarritoResponse>> actualizarCantidad(@PathVariable Long itemId,
            @Valid @RequestBody UpdateItemCarritoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cantidad actualizada correctamente", carritoService.actualizarCantidad(itemId, request)));
    }

    /** Elimina un único item del carrito. */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> eliminarItem(@PathVariable Long itemId) {
        carritoService.eliminarItem(itemId);
        return ResponseEntity.ok(ApiResponse.ok("Item eliminado correctamente", null));
    }

    /** Vacía el carrito sin modificar stock. */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> vaciarCarrito() {
        carritoService.vaciarCarrito();
        return ResponseEntity.ok(ApiResponse.ok("Carrito vaciado correctamente", null));
    }

    /** Convierte el carrito en una orden mediante la lógica transaccional existente. */
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(@Valid @RequestBody CheckoutCarritoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Pedido creado correctamente", carritoService.checkout(request)));
    }
}
