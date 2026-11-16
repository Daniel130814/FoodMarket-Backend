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
    void misSubPedidosDevuelveLosDelChefAutenticado() {
        User chef = new User();
        chef.setId(20L);
        chef.setRole(Role.CHEF);
        SubPedidoChef sub = new SubPedidoChef();
        when(authenticatedUserService.getCurrentUser()).thenReturn(chef);
        when(subPedidoChefRepository.findByChefId(20L)).thenReturn(java.util.List.of(sub));

        assertEquals(java.util.List.of(sub), subPedidoChefService.getMisSubPedidos());
    }

    @Test
    void misSubPedidosRechazaAQuienNoEsChef() {
        User admin = new User();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);
        when(authenticatedUserService.getCurrentUser()).thenReturn(admin);

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> subPedidoChefService.getMisSubPedidos());
    }

    @Test
    void cuentaPendientesYConfirmadosDelChefPropio() {
        User chef = new User();
        chef.setId(20L);
        chef.setRole(Role.CHEF);
        when(userRepository.existsById(20L)).thenReturn(true);
        when(authenticatedUserService.getCurrentUser()).thenReturn(chef);
        when(subPedidoChefRepository.countByChefIdAndEstadoIn(20L,
                java.util.List.of(EstadoPedido.PENDIENTE, EstadoPedido.CONFIRMADO))).thenReturn(3L);

        assertEquals(3L, subPedidoChefService.contarPendientesByChefId(20L));
        org.mockito.Mockito.verify(authenticatedUserService).requireOwnerOrAdmin(chef, 20L);
    }

    @Test
    void contarPendientesDeUnChefInexistenteDaNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        org.junit.jupiter.api.Assertions.assertThrows(
                com.uade.tpo.foodmarketplace.exceptions.user.UserNotFoundException.class,
                () -> subPedidoChefService.contarPendientesByChefId(99L));
    }

    @Test
    void contarPendientesDeOtroChefLanzaAccessDenied() {
        User otro = new User();
        otro.setId(5L);
        when(userRepository.existsById(20L)).thenReturn(true);
        when(authenticatedUserService.getCurrentUser()).thenReturn(otro);
        org.mockito.Mockito.doThrow(new org.springframework.security.access.AccessDeniedException("no"))
                .when(authenticatedUserService).requireOwnerOrAdmin(otro, 20L);

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> subPedidoChefService.contarPendientesByChefId(20L));
    }

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
