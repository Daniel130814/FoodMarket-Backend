package com.uade.tpo.foodmarketplace.service.carrito;

import com.uade.tpo.foodmarketplace.entity.dto.carrito.AddItemCarritoRequest;
import com.uade.tpo.foodmarketplace.entity.dto.carrito.CarritoResponse;
import com.uade.tpo.foodmarketplace.entity.dto.carrito.CheckoutCarritoRequest;
import com.uade.tpo.foodmarketplace.entity.dto.carrito.UpdateItemCarritoRequest;
import com.uade.tpo.foodmarketplace.entity.dto.order.OrderResponse;

public interface CarritoService {

    CarritoResponse getMiCarrito();

    CarritoResponse agregarItem(AddItemCarritoRequest request);

    CarritoResponse actualizarCantidad(Long itemId, UpdateItemCarritoRequest request);

    void eliminarItem(Long itemId);

    void vaciarCarrito();

    OrderResponse checkout(CheckoutCarritoRequest request);
}
