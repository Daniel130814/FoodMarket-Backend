package com.uade.tpo.foodmarketplace.service.order;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.foodmarketplace.entity.domicilio.Domicilio;
import com.uade.tpo.foodmarketplace.entity.order.DetallePedido;
import com.uade.tpo.foodmarketplace.entity.order.EstadoPedido;
import com.uade.tpo.foodmarketplace.entity.plato.EstadoPlato;
import com.uade.tpo.foodmarketplace.entity.order.Order;
import com.uade.tpo.foodmarketplace.entity.plato.Plato;
import com.uade.tpo.foodmarketplace.entity.order.SubPedidoChef;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.entity.dto.order.OrderRequest;
import com.uade.tpo.foodmarketplace.exceptions.common.BusinessRuleException;
import com.uade.tpo.foodmarketplace.exceptions.order.CantidadInvalidaException;
import com.uade.tpo.foodmarketplace.exceptions.order.InvalidOrderStateException;
import com.uade.tpo.foodmarketplace.exceptions.plato.PlatoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.order.PedidoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.domicilio.DomicilioNoPerteneceAlUsuarioException;
import com.uade.tpo.foodmarketplace.exceptions.domicilio.DomicilioNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.user.UserNotFoundException;
import com.uade.tpo.foodmarketplace.repository.domicilio.DomicilioRepository;
import com.uade.tpo.foodmarketplace.repository.order.OrderRepository;
import com.uade.tpo.foodmarketplace.repository.plato.PlatoRepository;
import com.uade.tpo.foodmarketplace.security.AuthenticatedUserService;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @Autowired
    private DomicilioRepository domicilioRepository;

    @Autowired
    private PlatoRepository platoRepository;

    @Override
    public List<Order> getOrders() {
        User currentUser = authenticatedUserService.getCurrentUser();
        return authenticatedUserService.isAdmin(currentUser)
                ? orderRepository.findAll()
                : orderRepository.findByUserId(currentUser.getId());
    }

    @Override
    public Optional<Order> getOrderById(Long orderId) {
        User currentUser = authenticatedUserService.getCurrentUser();
        Optional<Order> order = orderRepository.findById(orderId);
        order.ifPresent(value -> authenticatedUserService.requireOwnerOrAdmin(currentUser, value.getUser().getId()));
        return order;
    }

    /**
     * Crea una orden pendiente y agrupa sus detalles en un subpedido por chef.
     */
    @Override
    @Transactional
    public Order createOrder(OrderRequest request)
            throws UserNotFoundException {
        User user = authenticatedUserService.getCurrentUser();
        if (user.getRole() != com.uade.tpo.foodmarketplace.entity.user.Role.CLIENTE) {
            throw new BusinessRuleException("La compra debe corresponder a un cliente");
        }

        Domicilio domicilioEntrega = domicilioRepository.findById(request.getDomicilioEntregaId())
                .orElseThrow(DomicilioNotFoundException::new);

        if (!domicilioEntrega.getUsuario().getId().equals(user.getId())) {
            throw new DomicilioNoPerteneceAlUsuarioException();
        }
        Order order = new Order();
        order.setFechaCreacion(LocalDateTime.now());
        order.setEstado(EstadoPedido.PENDIENTE);
        order.setUser(user);
        order.setDomicilioEntrega(domicilioEntrega);
        BigDecimal total = BigDecimal.ZERO;
        Map<Long, SubPedidoChef> subPedidosPorChef = new LinkedHashMap<>();
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        request.getItems().forEach(item -> {
            if (item.getCantidad() == null || item.getCantidad() <= 0) throw new CantidadInvalidaException();
            cantidades.merge(item.getPlatoId(), item.getCantidad(), Integer::sum);
        });
        for (Map.Entry<Long, Integer> item : cantidades.entrySet()) {
            Plato plato = platoRepository.findByIdForUpdate(item.getKey()).orElseThrow(PlatoNotFoundException::new);
            if (plato.getEstado() != EstadoPlato.PUBLICADO) {
                throw new BusinessRuleException("El plato " + plato.getId() + " no esta publicado");
            }
            if (plato.getStockDisponible() < item.getValue()) {
                throw new BusinessRuleException("Stock insuficiente para el plato " + plato.getId());
            }
            plato.setStockDisponible(plato.getStockDisponible() - item.getValue());
            SubPedidoChef subPedido = subPedidosPorChef.computeIfAbsent(plato.getChef().getId(), ignored -> {
                SubPedidoChef nuevo = new SubPedidoChef();
                nuevo.setPedido(order); nuevo.setChef(plato.getChef()); nuevo.setEstado(EstadoPedido.PENDIENTE);
                nuevo.setSubtotal(BigDecimal.ZERO); order.getSubPedidos().add(nuevo); return nuevo;
            });
            DetallePedido detalle = new DetallePedido();
            detalle.setSubPedidoChef(subPedido); detalle.setPlato(plato); detalle.setCantidad(item.getValue());
            detalle.setPrecioUnitario(plato.getPrecio());
            detalle.setSubtotal(plato.getPrecio().multiply(BigDecimal.valueOf(item.getValue())));
            subPedido.getDetalles().add(detalle);
            subPedido.setSubtotal(subPedido.getSubtotal().add(detalle.getSubtotal()));
            total = total.add(detalle.getSubtotal());
        }
        order.setPrecioFinal(total);
        return orderRepository.save(order);
    }

    /**
     * Cancela una orden pendiente y repone el stock de sus ítems.
     */
    @Override
    @Transactional
    public Order cancelarOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(PedidoNotFoundException::new);

        authenticatedUserService.requireOwnerOrAdmin(
                authenticatedUserService.getCurrentUser(),
                order.getUser().getId()
        );

        if (order.getEstado() == EstadoPedido.CANCELADO) {
            return order;
        }

        if (order.getEstado() != EstadoPedido.PENDIENTE) {
            throw new InvalidOrderStateException(
                    "Solo se pueden cancelar órdenes pendientes"
            );
        }

        boolean todosPendientes = order.getSubPedidos().stream()
                .allMatch(sub -> sub.getEstado() == EstadoPedido.PENDIENTE);

        if (!todosPendientes) {
            throw new InvalidOrderStateException(
                    "La orden contiene subpedidos que ya comenzaron a procesarse"
            );
        }

        order.getSubPedidos().forEach(sub -> {
            sub.getDetalles().forEach(detalle -> {
                Plato plato = platoRepository.findByIdForUpdate(detalle.getPlato().getId())
                        .orElseThrow(PlatoNotFoundException::new);

                plato.setStockDisponible(
                        plato.getStockDisponible() + detalle.getCantidad()
                );
            });

            sub.setEstado(EstadoPedido.CANCELADO);
        });

        order.setEstado(EstadoPedido.CANCELADO);

        return orderRepository.save(order);
    }

    /**
     * Calcula el estado general utilizando el estado activo más avanzado de los subpedidos.
     * Una orden parcialmente entregada permanece ENVIADO hasta que todos los subpedidos estén ENTREGADO.
     */
    @Override
    public Order recalcularEstadoDesdeSubPedidos(Order order) {
        if (order.getEstado() == EstadoPedido.CANCELADO) {
            return order;
        }
        if (order.getSubPedidos().isEmpty()) {
            order.setEstado(EstadoPedido.PENDIENTE);
            return orderRepository.save(order);
        }
        if (order.getSubPedidos().stream().allMatch(sub -> sub.getEstado() == EstadoPedido.ENTREGADO)) {
            order.setEstado(EstadoPedido.ENTREGADO);
        } else if (order.getSubPedidos().stream().anyMatch(sub -> sub.getEstado() == EstadoPedido.ENVIADO
                || sub.getEstado() == EstadoPedido.ENTREGADO)) {
            // No existe un estado mixto en el enum, por lo que una entrega parcial se representa como ENVIADO.
            order.setEstado(EstadoPedido.ENVIADO);
        } else if (order.getSubPedidos().stream().anyMatch(sub -> sub.getEstado() == EstadoPedido.EN_PREPARACION)) {
            order.setEstado(EstadoPedido.EN_PREPARACION);
        } else if (order.getSubPedidos().stream().anyMatch(sub -> sub.getEstado() == EstadoPedido.CONFIRMADO)) {
            order.setEstado(EstadoPedido.CONFIRMADO);
        } else {
            order.setEstado(EstadoPedido.PENDIENTE);
        }

        return orderRepository.save(order);
    }
}
