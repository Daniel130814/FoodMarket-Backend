package com.uade.tpo.foodmarketplace.service.resena;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.uade.tpo.foodmarketplace.entity.order.EstadoPedido;
import com.uade.tpo.foodmarketplace.entity.plato.Plato;
import com.uade.tpo.foodmarketplace.entity.resena.Resena;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.exceptions.resena.ResenaDuplicateException;
import com.uade.tpo.foodmarketplace.repository.order.DetallePedidoRepository;
import com.uade.tpo.foodmarketplace.repository.plato.PlatoRepository;
import com.uade.tpo.foodmarketplace.repository.resena.ResenaRepository;
import com.uade.tpo.foodmarketplace.security.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class ResenaServiceImplTest {

    private static final long PLATO_ID = 5L;

    @Mock
    private ResenaRepository resenaRepository;

    @Mock
    private PlatoRepository platoRepository;

    @Mock
    private DetallePedidoRepository detallePedidoRepository;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private ResenaServiceImpl resenaService;

    @Test
    void creaResenaCuandoElClienteComproElPlatoEntregadoYNoTieneUnaPrevia() {
        User cliente = cliente(1L);
        Plato plato = plato();
        prepararCreacion(cliente, plato);
        when(resenaRepository.saveAndFlush(any(Resena.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Resena resultado = resenaService.createResena(5, "Muy rico", PLATO_ID);

        assertSame(cliente, resultado.getCliente());
        assertSame(plato, resultado.getPlato());
        assertEquals(5, resultado.getCalificacion());
        verify(resenaRepository).saveAndFlush(any(Resena.class));
    }

    @Test
    void rechazaUnaSegundaResenaDelMismoClienteParaElMismoPlato() {
        User cliente = cliente(1L);
        when(authenticatedUserService.getCurrentUser()).thenReturn(cliente);
        when(platoRepository.findById(PLATO_ID)).thenReturn(Optional.of(plato()));
        when(resenaRepository.existsByClienteIdAndPlatoId(cliente.getId(), PLATO_ID)).thenReturn(true);

        ResenaDuplicateException exception = assertThrows(ResenaDuplicateException.class,
                () -> resenaService.createResena(5, "Repetida", PLATO_ID));

        assertEquals(ResenaDuplicateException.MESSAGE, exception.getMessage());
        verify(resenaRepository, never()).saveAndFlush(any());
        verify(detallePedidoRepository, never()).existsBySubPedidoChefPedidoUserIdAndPlatoIdAndSubPedidoChefEstado(
                any(), any(), any(EstadoPedido.class));
    }

    @Test
    void permiteUnaResenaDeOtroClienteSobreElMismoPlatoCuandoCumpleLaCompra() {
        User primerCliente = cliente(1L);
        User segundoCliente = cliente(2L);
        Plato plato = plato();
        when(authenticatedUserService.getCurrentUser()).thenReturn(primerCliente, segundoCliente);
        when(platoRepository.findById(PLATO_ID)).thenReturn(Optional.of(plato));
        when(resenaRepository.existsByClienteIdAndPlatoId(1L, PLATO_ID)).thenReturn(false);
        when(resenaRepository.existsByClienteIdAndPlatoId(2L, PLATO_ID)).thenReturn(false);
        when(detallePedidoRepository.existsBySubPedidoChefPedidoUserIdAndPlatoIdAndSubPedidoChefEstado(
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.eq(PLATO_ID),
                org.mockito.ArgumentMatchers.eq(EstadoPedido.ENTREGADO))).thenReturn(true);
        when(resenaRepository.saveAndFlush(any(Resena.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Resena primera = resenaService.createResena(5, "Cliente uno", PLATO_ID);
        Resena segunda = resenaService.createResena(4, "Cliente dos", PLATO_ID);

        assertEquals(1L, primera.getCliente().getId());
        assertEquals(2L, segunda.getCliente().getId());
        verify(resenaRepository).existsByClienteIdAndPlatoId(1L, PLATO_ID);
        verify(resenaRepository).existsByClienteIdAndPlatoId(2L, PLATO_ID);
    }

    @Test
    void transformaLaViolacionDeLaConstraintDeResenaEnUnErrorDeConflicto() {
        User cliente = cliente(1L);
        prepararCreacion(cliente, plato());
        when(resenaRepository.saveAndFlush(any(Resena.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry for key uk_resena_cliente_plato"));

        ResenaDuplicateException exception = assertThrows(ResenaDuplicateException.class,
                () -> resenaService.createResena(5, "Concurrente", PLATO_ID));

        assertEquals(ResenaDuplicateException.MESSAGE, exception.getMessage());
    }

    private void prepararCreacion(User cliente, Plato plato) {
        when(authenticatedUserService.getCurrentUser()).thenReturn(cliente);
        when(platoRepository.findById(PLATO_ID)).thenReturn(Optional.of(plato));
        when(resenaRepository.existsByClienteIdAndPlatoId(cliente.getId(), PLATO_ID)).thenReturn(false);
        when(detallePedidoRepository.existsBySubPedidoChefPedidoUserIdAndPlatoIdAndSubPedidoChefEstado(
                cliente.getId(), PLATO_ID, EstadoPedido.ENTREGADO)).thenReturn(true);
    }

    private User cliente(Long id) {
        User cliente = new User();
        cliente.setId(id);
        return cliente;
    }

    private Plato plato() {
        Plato plato = new Plato();
        plato.setId(PLATO_ID);
        return plato;
    }
}
