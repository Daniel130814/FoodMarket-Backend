package com.uade.tpo.foodmarketplace.controllers.order;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.uade.tpo.foodmarketplace.entity.dto.order.EstadoSubPedidoRequest;
import com.uade.tpo.foodmarketplace.entity.order.EstadoPedido;
import com.uade.tpo.foodmarketplace.entity.order.SubPedidoChef;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.exceptions.order.SubPedidoNotFoundException;
import com.uade.tpo.foodmarketplace.service.order.SubPedidoChefService;

@ExtendWith(MockitoExtension.class)
class SubPedidosChefControllerTest {
    @Mock SubPedidoChefService service;
    @InjectMocks SubPedidosChefController controller;

    @Test void listasVaciasDevuelvenDataNullYMensajeEspecifico() {
        when(service.getSubPedidosByOrderId(1L)).thenReturn(List.of());
        when(service.getSubPedidosByChefId(2L)).thenReturn(List.of());
        var porPedido = controller.getByOrder(1L);
        var porChef = controller.getByChef(2L);
        assertEquals(HttpStatus.OK, porPedido.getStatusCode());
        assertTrue(porPedido.getBody().success());
        assertEquals("No hay subpedidos disponibles para este pedido", porPedido.getBody().message());
        assertNull(porPedido.getBody().data());
        assertEquals("No hay subpedidos disponibles para este chef", porChef.getBody().message());
        assertNull(porChef.getBody().data());
    }

    @Test void listaConDatosYPatchDevuelvenDtoEnvuelto() {
        SubPedidoChef subPedido = subPedido();
        when(service.getSubPedidosByOrderId(1L)).thenReturn(List.of(subPedido));
        when(service.actualizarEstado(5L, EstadoPedido.CONFIRMADO)).thenReturn(subPedido);
        assertEquals(5L, controller.getByOrder(1L).getBody().data().getFirst().id());
        EstadoSubPedidoRequest request = new EstadoSubPedidoRequest();
        request.setEstado(EstadoPedido.CONFIRMADO);
        var response = controller.actualizarEstado(5L, request);
        assertTrue(response.getBody().success());
        assertEquals("Estado del subpedido actualizado correctamente", response.getBody().message());
        assertEquals(5L, response.getBody().data().id());
    }

    @Test void subPedidoInexistenteLanzaNotFoundDelDominio() {
        when(service.getSubPedidoById(99L)).thenReturn(Optional.empty());
        assertThrows(SubPedidoNotFoundException.class, () -> controller.getById(99L));
    }

    private SubPedidoChef subPedido() {
        User chef = new User();
        chef.setId(2L);
        chef.setNombre("Chef");
        SubPedidoChef subPedido = new SubPedidoChef();
        subPedido.setId(5L);
        subPedido.setChef(chef);
        subPedido.setEstado(EstadoPedido.CONFIRMADO);
        subPedido.setSubtotal(BigDecimal.TEN);
        return subPedido;
    }
}
