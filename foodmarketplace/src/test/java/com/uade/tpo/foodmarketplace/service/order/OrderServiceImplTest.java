package com.uade.tpo.foodmarketplace.service.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.tpo.foodmarketplace.entity.order.DetallePedido;
import com.uade.tpo.foodmarketplace.entity.order.EstadoPedido;
import com.uade.tpo.foodmarketplace.entity.order.Order;
import com.uade.tpo.foodmarketplace.entity.order.SubPedidoChef;
import com.uade.tpo.foodmarketplace.entity.plato.EstadoPlato;
import com.uade.tpo.foodmarketplace.entity.plato.Plato;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.exceptions.order.InvalidOrderStateException;
import com.uade.tpo.foodmarketplace.repository.domicilio.DomicilioRepository;
import com.uade.tpo.foodmarketplace.repository.order.OrderRepository;
import com.uade.tpo.foodmarketplace.repository.order.SubPedidoChefRepository;
import com.uade.tpo.foodmarketplace.repository.plato.PlatoRepository;
import com.uade.tpo.foodmarketplace.security.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private PlatoRepository platoRepository;
    @Mock private SubPedidoChefRepository subPedidoChefRepository;
    @Mock private AuthenticatedUserService authenticatedUserService;
    @Mock private DomicilioRepository domicilioRepository;
    @InjectMocks private OrderServiceImpl orderService;

    @Test
    void segundaCancelacionNoRestauraStockYDevuelveConflicto() {
        User cliente = new User();
        cliente.setId(1L);
        Order order = order(cliente, EstadoPedido.CANCELADO);
        when(orderRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(order));
        when(authenticatedUserService.getCurrentUser()).thenReturn(cliente);

        assertThrows(InvalidOrderStateException.class, () -> orderService.cancelarOrder(10L));

        verify(orderRepository).findByIdForUpdate(10L);
        verify(platoRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void cancelacionBloqueadaRestauraUnaSolaVezLaCantidadDelDetalle() {
        User cliente = new User();
        cliente.setId(1L);
        Order order = order(cliente, EstadoPedido.PENDIENTE);
        Plato plato = new Plato();
        plato.setId(5L);
        plato.setEstado(EstadoPlato.AGOTADO);
        plato.setStockDisponible(8);
        DetallePedido detalle = new DetallePedido();
        detalle.setPlato(plato);
        detalle.setCantidad(2);
        SubPedidoChef sub = order.getSubPedidos().getFirst();
        sub.getDetalles().add(detalle);
        when(orderRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(order));
        when(authenticatedUserService.getCurrentUser()).thenReturn(cliente);
        when(platoRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(plato));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.cancelarOrder(10L);

        assertEquals(10, plato.getStockDisponible());
        assertEquals(EstadoPedido.CANCELADO, order.getEstado());
        assertEquals(EstadoPedido.CANCELADO, sub.getEstado());
    }

    private Order order(User cliente, EstadoPedido estado) {
        Order order = new Order();
        order.setId(10L);
        order.setUser(cliente);
        order.setEstado(estado);
        order.setPrecioFinal(BigDecimal.TEN);
        SubPedidoChef sub = new SubPedidoChef();
        sub.setEstado(EstadoPedido.PENDIENTE);
        sub.setPedido(order);
        order.getSubPedidos().add(sub);
        return order;
    }
}
