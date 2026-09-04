package com.uade.tpo.foodmarketplace.service;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.foodmarketplace.entity.Domicilio;
import com.uade.tpo.foodmarketplace.entity.DetallePedido;
import com.uade.tpo.foodmarketplace.entity.EstadoPedido;
import com.uade.tpo.foodmarketplace.entity.EstadoPlato;
import com.uade.tpo.foodmarketplace.entity.Order;
import com.uade.tpo.foodmarketplace.entity.Plato;
import com.uade.tpo.foodmarketplace.entity.SubPedidoChef;
import com.uade.tpo.foodmarketplace.entity.User;
import com.uade.tpo.foodmarketplace.entity.dto.OrderRequest;
import com.uade.tpo.foodmarketplace.exceptions.BusinessRuleException;
import com.uade.tpo.foodmarketplace.exceptions.CantidadInvalidaException;
import com.uade.tpo.foodmarketplace.exceptions.PlatoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.PedidoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.DomicilioNoPerteneceAlUsuarioException;
import com.uade.tpo.foodmarketplace.exceptions.DomicilioNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.UserNotFoundException;
import com.uade.tpo.foodmarketplace.repository.DomicilioRepository;
import com.uade.tpo.foodmarketplace.repository.OrderRepository;
import com.uade.tpo.foodmarketplace.repository.UserRepository;
import com.uade.tpo.foodmarketplace.repository.PlatoRepository;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DomicilioRepository domicilioRepository;

    @Autowired
    private PlatoRepository platoRepository;

    @Override
    public List<Order> getOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    @Transactional
    public Order createOrder(OrderRequest request)
            throws UserNotFoundException {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(UserNotFoundException::new);
        if (user.getRole() != com.uade.tpo.foodmarketplace.entity.Role.CLIENTE) {
            throw new BusinessRuleException("La compra debe corresponder a un cliente");
        }

        Domicilio domicilioEntrega = domicilioRepository.findById(request.getDomicilioEntregaId())
                .orElseThrow(DomicilioNotFoundException::new);

        if (!domicilioEntrega.getUsuario().getId().equals(request.getUserId())) {
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

    @Override
    @Transactional
    public Order cancelarOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(PedidoNotFoundException::new);
        if (order.getEstado() == EstadoPedido.CANCELADO) return order;
        if (order.getSubPedidos().stream().anyMatch(s -> s.getEstado() == EstadoPedido.ENTREGADO)) {
            throw new BusinessRuleException("No se puede cancelar una orden con subpedidos entregados");
        }
        order.getSubPedidos().forEach(sub -> {
            sub.getDetalles().forEach(detalle -> {
                Plato plato = platoRepository.findByIdForUpdate(detalle.getPlato().getId()).orElseThrow(PlatoNotFoundException::new);
                plato.setStockDisponible(plato.getStockDisponible() + detalle.getCantidad());
            });
            sub.setEstado(EstadoPedido.CANCELADO);
        });
        order.setEstado(EstadoPedido.CANCELADO);
        return order;
    }
}
