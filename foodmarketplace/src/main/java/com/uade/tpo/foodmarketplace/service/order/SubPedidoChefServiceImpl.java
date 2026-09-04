package com.uade.tpo.foodmarketplace.service.order;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.foodmarketplace.entity.order.EstadoPedido;
import com.uade.tpo.foodmarketplace.entity.order.Order;
import com.uade.tpo.foodmarketplace.entity.order.SubPedidoChef;
import com.uade.tpo.foodmarketplace.exceptions.order.InvalidOrderStateException;
import com.uade.tpo.foodmarketplace.exceptions.order.InvalidSubPedidoStateException;
import com.uade.tpo.foodmarketplace.exceptions.order.PedidoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.order.SubPedidoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.user.UserNotFoundException;
import com.uade.tpo.foodmarketplace.repository.order.OrderRepository;
import com.uade.tpo.foodmarketplace.repository.order.SubPedidoChefRepository;
import com.uade.tpo.foodmarketplace.repository.user.UserRepository;

@Service
public class SubPedidoChefServiceImpl implements SubPedidoChefService {

    private final SubPedidoChefRepository subPedidoChefRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;

    public SubPedidoChefServiceImpl(SubPedidoChefRepository subPedidoChefRepository, OrderRepository orderRepository,
            UserRepository userRepository, OrderService orderService) {
        this.subPedidoChefRepository = subPedidoChefRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderService = orderService;
    }

    /**
     * Busca un subpedido sin exponer el repositorio directamente al controlador.
     */
    @Override
    public Optional<SubPedidoChef> getSubPedidoById(Long subPedidoId) {
        return subPedidoChefRepository.findById(subPedidoId);
    }

    /**
     * Lista los subpedidos de una orden tras verificar que la orden padre existe.
     */
    @Override
    public List<SubPedidoChef> getSubPedidosByOrderId(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new PedidoNotFoundException();
        }

        return subPedidoChefRepository.findByPedidoId(orderId);
    }

    /**
     * Lista el trabajo asignado a un chef tras verificar que el usuario solicitado existe.
     */
    @Override
    public List<SubPedidoChef> getSubPedidosByChefId(Long chefId) {
        if (!userRepository.existsById(chefId)) {
            throw new UserNotFoundException();
        }

        return subPedidoChefRepository.findByChefId(chefId);
    }

    /**
     * Actualiza el estado de un subpedido y luego recalcula el estado de su orden padre.
     */
    @Override
    @Transactional
    public SubPedidoChef actualizarEstado(Long subPedidoId, EstadoPedido nuevoEstado) {
        SubPedidoChef subPedido = subPedidoChefRepository.findById(subPedidoId)
                .orElseThrow(SubPedidoNotFoundException::new);
        Order order = subPedido.getPedido();

        // Una compra pendiente no puede entrar en preparación antes de que un pago la confirme.
        validarOrdenHabilitada(order);
        // La máquina de estados evita transiciones salteadas, inversas o desde estados terminales.
        validarTransicion(subPedido.getEstado(), nuevoEstado);
        subPedido.setEstado(nuevoEstado);

        // Tras modificar el subpedido independiente, se deriva nuevamente el estado general de la orden.
        orderService.recalcularEstadoDesdeSubPedidos(order);
        return subPedidoChefRepository.save(subPedido);
    }

    /**
     * Verifica que la orden padre esté confirmada y no tenga un estado terminal antes de avanzar.
     */
    private void validarOrdenHabilitada(Order order) {
        if (order.getEstado() == EstadoPedido.CANCELADO) {
            throw new InvalidOrderStateException("No se puede modificar un subpedido de una orden cancelada");
        }
        if (order.getEstado() == EstadoPedido.ENTREGADO) {
            throw new InvalidOrderStateException("No se puede modificar un subpedido de una orden entregada");
        }
        if (order.getEstado() == EstadoPedido.PENDIENTE) {
            throw new InvalidOrderStateException("La orden debe estar confirmada antes de avanzar un subpedido");
        }
    }

    /**
     * Valida la única transición hacia adelante permitida para cada estado de subpedido.
     */
    private void validarTransicion(EstadoPedido estadoActual, EstadoPedido nuevoEstado) {
        boolean esValida = switch (estadoActual) {
            case PENDIENTE -> nuevoEstado == EstadoPedido.CONFIRMADO;
            case CONFIRMADO -> nuevoEstado == EstadoPedido.EN_PREPARACION;
            case EN_PREPARACION -> nuevoEstado == EstadoPedido.ENVIADO;
            case ENVIADO -> nuevoEstado == EstadoPedido.ENTREGADO;
            case ENTREGADO, CANCELADO -> false;
        };

        if (!esValida) {
            throw new InvalidSubPedidoStateException(
                    "Transicion invalida de " + estadoActual + " a " + nuevoEstado + " para el subpedido");
        }
    }
}
