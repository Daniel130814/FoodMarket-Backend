package com.uade.tpo.foodmarketplace.service.carrito;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.foodmarketplace.entity.carrito.Carrito;
import com.uade.tpo.foodmarketplace.entity.carrito.ItemCarrito;
import com.uade.tpo.foodmarketplace.entity.dto.carrito.AddItemCarritoRequest;
import com.uade.tpo.foodmarketplace.entity.dto.carrito.CarritoResponse;
import com.uade.tpo.foodmarketplace.entity.dto.carrito.CheckoutCarritoRequest;
import com.uade.tpo.foodmarketplace.entity.dto.carrito.ItemCarritoResponse;
import com.uade.tpo.foodmarketplace.entity.dto.carrito.UpdateItemCarritoRequest;
import com.uade.tpo.foodmarketplace.entity.dto.common.ResponseMapper;
import com.uade.tpo.foodmarketplace.entity.dto.order.OrderItemRequest;
import com.uade.tpo.foodmarketplace.entity.dto.order.OrderRequest;
import com.uade.tpo.foodmarketplace.entity.dto.order.OrderResponse;
import com.uade.tpo.foodmarketplace.entity.plato.EstadoPlato;
import com.uade.tpo.foodmarketplace.entity.plato.Plato;
import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.exceptions.carrito.CarritoVacioException;
import com.uade.tpo.foodmarketplace.exceptions.carrito.ItemCarritoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.common.BusinessRuleException;
import com.uade.tpo.foodmarketplace.exceptions.plato.PlatoNotFoundException;
import com.uade.tpo.foodmarketplace.repository.carrito.CarritoRepository;
import com.uade.tpo.foodmarketplace.repository.carrito.ItemCarritoRepository;
import com.uade.tpo.foodmarketplace.repository.plato.PlatoRepository;
import com.uade.tpo.foodmarketplace.security.AuthenticatedUserService;
import com.uade.tpo.foodmarketplace.service.order.OrderService;
import com.uade.tpo.foodmarketplace.service.plato.PrecioPlatoCalculator;

/** Gestiona el carrito del cliente autenticado; el stock solo se descuenta al checkout. */
@Service
public class CarritoServiceImpl implements CarritoService {

    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final PlatoRepository platoRepository;
    private final OrderService orderService;
    private final AuthenticatedUserService authenticatedUserService;

    public CarritoServiceImpl(CarritoRepository carritoRepository, ItemCarritoRepository itemCarritoRepository,
            PlatoRepository platoRepository, OrderService orderService, AuthenticatedUserService authenticatedUserService) {
        this.carritoRepository = carritoRepository;
        this.itemCarritoRepository = itemCarritoRepository;
        this.platoRepository = platoRepository;
        this.orderService = orderService;
        this.authenticatedUserService = authenticatedUserService;
    }

    /** Obtiene el carrito del JWT o crea uno vacío de forma lazy para el cliente. */
    @Override
    @Transactional
    public CarritoResponse getMiCarrito() {
        return toResponse(obtenerOCrearCarrito());
    }

    /** Agrega un plato o suma su cantidad existente sin reservar ni descontar stock. */
    @Override
    @Transactional
    public CarritoResponse agregarItem(AddItemCarritoRequest request) {
        Carrito carrito = obtenerOCrearCarrito();
        Plato plato = obtenerPlatoDisponible(request.platoId());
        ItemCarrito item = itemCarritoRepository.findByCarritoIdAndPlatoId(carrito.getId(), plato.getId())
                .orElseGet(() -> nuevoItem(carrito, plato));
        int cantidadFinal = item.getCantidad() == null ? request.cantidad() : item.getCantidad() + request.cantidad();
        validarCantidadContraStock(plato, cantidadFinal);
        item.setCantidad(cantidadFinal);
        if (item.getId() == null) {
            carrito.getItems().add(item);
        }
        itemCarritoRepository.save(item);
        return toResponse(carrito);
    }

    /** Actualiza una cantidad solo si el item pertenece al carrito del cliente autenticado. */
    @Override
    @Transactional
    public CarritoResponse actualizarCantidad(Long itemId, UpdateItemCarritoRequest request) {
        Carrito carrito = obtenerOCrearCarrito();
        ItemCarrito item = obtenerItemPropio(itemId, carrito);
        validarCantidadContraStock(obtenerPlatoDisponible(item.getPlato().getId()), request.cantidad());
        item.setCantidad(request.cantidad());
        itemCarritoRepository.save(item);
        return toResponse(carrito);
    }

    /** Elimina un único item del carrito autenticado sin modificar el stock. */
    @Override
    @Transactional
    public void eliminarItem(Long itemId) {
        ItemCarrito item = obtenerItemPropio(itemId, obtenerOCrearCarrito());
        itemCarritoRepository.delete(item);
    }

    /** Vacía los items conservando la entidad Carrito para el mismo cliente. */
    @Override
    @Transactional
    public void vaciarCarrito() {
        Carrito carrito = obtenerOCrearCarrito();
        carrito.getItems().clear();
        carritoRepository.save(carrito);
    }

    /**
     * Reutiliza OrderService para recalcular stock y precios al confirmar el checkout.
     */
    @Override
    @Transactional
    public OrderResponse checkout(CheckoutCarritoRequest request) {
        Carrito carrito = obtenerOCrearCarrito();
        if (carrito.getItems().isEmpty()) {
            throw new CarritoVacioException();
        }

        // El carrito no congela precios ni stock: OrderService los vuelve a validar dentro de la transacción.
        List<OrderItemRequest> items = carrito.getItems().stream().map(item -> {
            OrderItemRequest orderItem = new OrderItemRequest();
            orderItem.setPlatoId(item.getPlato().getId());
            orderItem.setCantidad(item.getCantidad());
            return orderItem;
        }).toList();
        OrderRequest orderRequest = new OrderRequest(request.domicilioEntregaId(), items);
        var order = orderService.createOrder(orderRequest);
        // Solo se vacía si la orden fue creada; una excepción revierte toda la transacción.
        carrito.getItems().clear();
        carritoRepository.save(carrito);
        return ResponseMapper.order(order);
    }

    private Carrito obtenerOCrearCarrito() {
        User cliente = obtenerClienteActual();
        return carritoRepository.findByClienteId(cliente.getId()).orElseGet(() -> {
            Carrito carrito = new Carrito();
            carrito.setCliente(cliente);
            return carritoRepository.save(carrito);
        });
    }

    private User obtenerClienteActual() {
        User user = authenticatedUserService.getCurrentUser();
        if (user.getRole() != Role.CLIENTE) {
            throw new AccessDeniedException("Solo un CLIENTE puede utilizar el carrito");
        }
        return user;
    }

    private Plato obtenerPlatoDisponible(Long platoId) {
        Plato plato = platoRepository.findById(platoId).orElseThrow(PlatoNotFoundException::new);
        if (plato.getEstado() != EstadoPlato.PUBLICADO || plato.getStockDisponible() <= 0) {
            throw new BusinessRuleException("El plato no está disponible para agregar al carrito");
        }
        return plato;
    }

    private void validarCantidadContraStock(Plato plato, int cantidad) {
        if (cantidad <= 0 || cantidad > plato.getStockDisponible()) {
            throw new BusinessRuleException("La cantidad solicitada no tiene stock suficiente");
        }
    }

    private ItemCarrito nuevoItem(Carrito carrito, Plato plato) {
        ItemCarrito item = new ItemCarrito();
        item.setCarrito(carrito);
        item.setPlato(plato);
        return item;
    }

    private ItemCarrito obtenerItemPropio(Long itemId, Carrito carrito) {
        ItemCarrito item = itemCarritoRepository.findById(itemId).orElseThrow(ItemCarritoNotFoundException::new);
        if (!item.getCarrito().getId().equals(carrito.getId())) {
            throw new AccessDeniedException("No podés modificar items de otro carrito");
        }
        return item;
    }

    private CarritoResponse toResponse(Carrito carrito) {
        List<ItemCarritoResponse> items = carrito.getItems().stream().map(this::toItemResponse).toList();
        BigDecimal total = items.stream().map(ItemCarritoResponse::subtotalEstimado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CarritoResponse(carrito.getId(), items, total);
    }

    private ItemCarritoResponse toItemResponse(ItemCarrito item) {
        Plato plato = item.getPlato();
        BigDecimal precioActual = PrecioPlatoCalculator.precioEfectivo(plato);
        return new ItemCarritoResponse(item.getId(), plato.getId(), plato.getNombre(), item.getCantidad(), plato.getPrecio(),
                plato.getDescuentoPorcentaje(), precioActual, precioActual.multiply(BigDecimal.valueOf(item.getCantidad())));
    }
}
