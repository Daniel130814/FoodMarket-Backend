package com.uade.tpo.foodmarketplace.service.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.tpo.foodmarketplace.entity.order.EstadoPedido;
import com.uade.tpo.foodmarketplace.entity.order.Order;
import com.uade.tpo.foodmarketplace.entity.order.SubPedidoChef;
import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.repository.order.OrderRepository;
import com.uade.tpo.foodmarketplace.repository.order.SubPedidoChefRepository;
import com.uade.tpo.foodmarketplace.repository.user.UserRepository;
import com.uade.tpo.foodmarketplace.security.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class SubPedidoChefServiceImplTest {

    @Mock private SubPedidoChefRepository subPedidoChefRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrderService orderService;
    @Mock private AuthenticatedUserService authenticatedUserService;
    @InjectMocks private SubPedidoChefServiceImpl subPedidoChefService;

    @Test
    void bloqueaLaOrderAntesDeModificarYRecalcularElSubPedido() {
        User chef = new User();
        chef.setId(20L);
        chef.setRole(Role.CHEF);
        Order order = new Order();
        order.setId(60L);
        order.setEstado(EstadoPedido.CONFIRMADO);
        SubPedidoChef subPedido = new SubPedidoChef();
        subPedido.setId(7L);
        subPedido.setPedido(order);
        subPedido.setChef(chef);
        subPedido.setEstado(EstadoPedido.CONFIRMADO);

        when(subPedidoChefRepository.findPedidoIdById(7L)).thenReturn(Optional.of(60L));
        when(orderRepository.findByIdForUpdate(60L)).thenReturn(Optional.of(order));
        when(subPedidoChefRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(subPedido));
        when(authenticatedUserService.getCurrentUser()).thenReturn(chef);
        when(subPedidoChefRepository.saveAndFlush(subPedido)).thenReturn(subPedido);

        SubPedidoChef result = subPedidoChefService.actualizarEstado(7L, EstadoPedido.EN_PREPARACION);

        assertEquals(EstadoPedido.EN_PREPARACION, result.getEstado());
        InOrder orden = inOrder(subPedidoChefRepository, orderRepository, orderService);
        orden.verify(subPedidoChefRepository).findPedidoIdById(7L);
        orden.verify(orderRepository).findByIdForUpdate(60L);
        orden.verify(subPedidoChefRepository).findByIdForUpdate(7L);
        orden.verify(subPedidoChefRepository).saveAndFlush(subPedido);
        orden.verify(orderService).recalcularEstadoDesdeSubPedidos(order);
    }
}
