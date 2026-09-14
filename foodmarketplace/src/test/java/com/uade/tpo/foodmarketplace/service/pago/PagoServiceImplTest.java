package com.uade.tpo.foodmarketplace.service.pago;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.tpo.foodmarketplace.entity.order.EstadoPedido;
import com.uade.tpo.foodmarketplace.entity.order.Order;
import com.uade.tpo.foodmarketplace.entity.pago.EstadoPago;
import com.uade.tpo.foodmarketplace.entity.pago.Pago;
import com.uade.tpo.foodmarketplace.exceptions.common.BusinessRuleException;
import com.uade.tpo.foodmarketplace.exceptions.pago.InvalidPagoStateException;
import com.uade.tpo.foodmarketplace.repository.order.OrderRepository;
import com.uade.tpo.foodmarketplace.repository.pago.PagoRepository;
import com.uade.tpo.foodmarketplace.security.AuthenticatedUserService;
import com.uade.tpo.foodmarketplace.service.order.OrderService;

@ExtendWith(MockitoExtension.class)
class PagoServiceImplTest {

    private static final long ORDER_ID = 50L;
    private static final long PAGO_A_ID = 100L;
    private static final long PAGO_B_ID = 101L;

    @Mock private PagoRepository pagoRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderService orderService;
    @Mock private AuthenticatedUserService authenticatedUserService;
    @InjectMocks private PagoServiceImpl pagoService;

    @Test
    void apruebaUnPagoSoloDespuesDeBloquearLaOrder() {
        Order order = order();
        Pago pago = pago(PAGO_A_ID, order, EstadoPago.PENDIENTE);
        prepararActualizacion(PAGO_A_ID, order, pago);
        when(pagoRepository.existsByPedidoIdAndEstado(ORDER_ID, EstadoPago.APROBADO)).thenReturn(false);
        when(pagoRepository.save(pago)).thenReturn(pago);

        Pago resultado = pagoService.actualizarEstadoPago(PAGO_A_ID, EstadoPago.APROBADO);

        assertEquals(EstadoPago.APROBADO, resultado.getEstado());
        InOrder orden = inOrder(pagoRepository, orderRepository);
        orden.verify(pagoRepository).findPedidoIdById(PAGO_A_ID);
        orden.verify(orderRepository).findByIdForUpdate(ORDER_ID);
        orden.verify(pagoRepository).findByIdForUpdate(PAGO_A_ID);
        verify(orderService).recalcularEstadoDesdeSubPedidos(order);
    }

    @Test
    void rechazaUnSegundoPagoAprobadoDespuesDeVolverAValidarBajoElLock() {
        Order order = order();
        Pago pago = pago(PAGO_B_ID, order, EstadoPago.PENDIENTE);
        prepararActualizacion(PAGO_B_ID, order, pago);
        when(pagoRepository.existsByPedidoIdAndEstado(ORDER_ID, EstadoPago.APROBADO)).thenReturn(true);

        InvalidPagoStateException exception = assertThrows(InvalidPagoStateException.class,
                () -> pagoService.actualizarEstadoPago(PAGO_B_ID, EstadoPago.APROBADO));

        assertEquals("La orden ya posee un pago aprobado", exception.getMessage());
        verify(orderRepository).findByIdForUpdate(ORDER_ID);
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void quintoRechazoBloqueaLaOrderYNoPermiteUnSexto() {
        Order order = order();
        Pago quinto = pago(PAGO_A_ID, order, EstadoPago.PENDIENTE);
        prepararActualizacion(PAGO_A_ID, order, quinto);
        when(pagoRepository.countByPedidoIdAndEstado(ORDER_ID, EstadoPago.RECHAZADO)).thenReturn(4L);
        when(pagoRepository.save(quinto)).thenReturn(quinto);

        pagoService.actualizarEstadoPago(PAGO_A_ID, EstadoPago.RECHAZADO);

        assertEquals(EstadoPago.RECHAZADO, quinto.getEstado());
        assertEquals(true, order.isPagoBloqueado());

        Pago sexto = pago(PAGO_B_ID, order, EstadoPago.PENDIENTE);
        prepararActualizacion(PAGO_B_ID, order, sexto);
        when(pagoRepository.countByPedidoIdAndEstado(ORDER_ID, EstadoPago.RECHAZADO)).thenReturn(5L);

        assertThrows(BusinessRuleException.class, () -> pagoService.actualizarEstadoPago(PAGO_B_ID, EstadoPago.RECHAZADO));
        assertEquals(EstadoPago.PENDIENTE, sexto.getEstado());
    }

    private void prepararActualizacion(Long pagoId, Order order, Pago pago) {
        when(pagoRepository.findPedidoIdById(pagoId)).thenReturn(Optional.of(ORDER_ID));
        when(orderRepository.findByIdForUpdate(ORDER_ID)).thenReturn(Optional.of(order));
        when(pagoRepository.findByIdForUpdate(pagoId)).thenReturn(Optional.of(pago));
    }

    private Order order() {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setEstado(EstadoPedido.PENDIENTE);
        order.setPrecioFinal(BigDecimal.TEN);
        return order;
    }

    private Pago pago(Long id, Order order, EstadoPago estado) {
        Pago pago = new Pago();
        pago.setId(id);
        pago.setPedido(order);
        pago.setEstado(estado);
        return pago;
    }
}
