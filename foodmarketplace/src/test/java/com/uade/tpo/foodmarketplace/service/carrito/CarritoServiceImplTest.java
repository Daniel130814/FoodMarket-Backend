package com.uade.tpo.foodmarketplace.service.carrito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.tpo.foodmarketplace.entity.carrito.Carrito;
import com.uade.tpo.foodmarketplace.entity.carrito.ItemCarrito;
import com.uade.tpo.foodmarketplace.entity.dto.carrito.AddItemCarritoRequest;
import com.uade.tpo.foodmarketplace.entity.plato.EstadoPlato;
import com.uade.tpo.foodmarketplace.entity.plato.Plato;
import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.repository.carrito.CarritoRepository;
import com.uade.tpo.foodmarketplace.repository.carrito.ItemCarritoRepository;
import com.uade.tpo.foodmarketplace.repository.plato.PlatoRepository;
import com.uade.tpo.foodmarketplace.repository.user.UserRepository;
import com.uade.tpo.foodmarketplace.security.AuthenticatedUserService;
import com.uade.tpo.foodmarketplace.service.order.OrderService;

@ExtendWith(MockitoExtension.class)
class CarritoServiceImplTest {

    private static final Long CLIENTE_ID = 1L;
    private static final Long CARRITO_ID = 2L;
    private static final Long PLATO_ID = 3L;

    @Mock private CarritoRepository carritoRepository;
    @Mock private ItemCarritoRepository itemCarritoRepository;
    @Mock private PlatoRepository platoRepository;
    @Mock private OrderService orderService;
    @Mock private AuthenticatedUserService authenticatedUserService;
    @Mock private UserRepository userRepository;
    @InjectMocks private CarritoServiceImpl carritoService;

    @Test
    void creaElCarritoLazyBajoElLockDelClienteYEnLaMismaTransaccion() {
        User cliente = cliente();
        Carrito carrito = carrito(cliente);
        when(authenticatedUserService.getCurrentUser()).thenReturn(cliente);
        when(carritoRepository.findByClienteId(CLIENTE_ID)).thenReturn(Optional.empty());
        when(userRepository.findByIdForUpdate(CLIENTE_ID)).thenReturn(Optional.of(cliente));
        when(carritoRepository.findByClienteIdForUpdate(CLIENTE_ID))
                .thenReturn(Optional.empty());
        when(carritoRepository.saveAndFlush(any(Carrito.class))).thenReturn(carrito);

        var respuesta = carritoService.getMiCarrito();

        assertEquals(CARRITO_ID, respuesta.id());
        InOrder orden = inOrder(carritoRepository, userRepository);
        orden.verify(carritoRepository).findByClienteId(CLIENTE_ID);
        orden.verify(userRepository).findByIdForUpdate(CLIENTE_ID);
        orden.verify(carritoRepository).findByClienteIdForUpdate(CLIENTE_ID);
        orden.verify(carritoRepository).saveAndFlush(any(Carrito.class));
    }

    @Test
    void usaElCarritoCreadoPorOtraRequestDespuesDelLockDelCliente() {
        User cliente = cliente();
        Carrito carrito = carrito(cliente);
        when(authenticatedUserService.getCurrentUser()).thenReturn(cliente);
        when(carritoRepository.findByClienteId(CLIENTE_ID)).thenReturn(Optional.empty());
        when(userRepository.findByIdForUpdate(CLIENTE_ID)).thenReturn(Optional.of(cliente));
        when(carritoRepository.findByClienteIdForUpdate(CLIENTE_ID))
                .thenReturn(Optional.of(carrito));

        var respuesta = carritoService.getMiCarrito();

        assertEquals(CARRITO_ID, respuesta.id());
        verify(carritoRepository, never()).saveAndFlush(any(Carrito.class));
    }

    @Test
    void sumaDosAgregadosDelMismoPlatoBajoElLockDelCarrito() {
        User cliente = cliente();
        Carrito carrito = carrito(cliente);
        Plato plato = plato();
        ItemCarrito item = new ItemCarrito();
        item.setId(4L);
        item.setCarrito(carrito);
        item.setPlato(plato);
        item.setCantidad(1);
        carrito.getItems().add(item);
        when(authenticatedUserService.getCurrentUser()).thenReturn(cliente);
        when(carritoRepository.findByClienteId(CLIENTE_ID)).thenReturn(Optional.of(carrito));
        when(carritoRepository.findByClienteIdForUpdate(CLIENTE_ID)).thenReturn(Optional.of(carrito));
        when(platoRepository.findById(PLATO_ID)).thenReturn(Optional.of(plato));
        when(itemCarritoRepository.findByCarritoIdAndPlatoIdForUpdate(CARRITO_ID, PLATO_ID)).thenReturn(Optional.of(item));

        var respuesta = carritoService.agregarItem(new AddItemCarritoRequest(PLATO_ID, 1));

        assertEquals(2, item.getCantidad());
        assertEquals(2, respuesta.items().getFirst().cantidad());
        InOrder orden = inOrder(carritoRepository, itemCarritoRepository);
        orden.verify(carritoRepository).findByClienteId(CLIENTE_ID);
        orden.verify(carritoRepository).findByClienteIdForUpdate(CLIENTE_ID);
        orden.verify(itemCarritoRepository).findByCarritoIdAndPlatoIdForUpdate(CARRITO_ID, PLATO_ID);
        orden.verify(itemCarritoRepository).saveAndFlush(item);
        verify(userRepository, never()).findByIdForUpdate(CLIENTE_ID);
    }

    @Test
    void dosAgregadosLogicosDelMismoPlatoConservanUnSoloItem() {
        User cliente = cliente();
        Carrito carrito = carrito(cliente);
        Plato plato = plato();
        AtomicReference<ItemCarrito> itemCreado = new AtomicReference<>();
        when(authenticatedUserService.getCurrentUser()).thenReturn(cliente);
        when(carritoRepository.findByClienteId(CLIENTE_ID)).thenReturn(Optional.of(carrito));
        when(carritoRepository.findByClienteIdForUpdate(CLIENTE_ID)).thenReturn(Optional.of(carrito));
        when(platoRepository.findById(PLATO_ID)).thenReturn(Optional.of(plato));
        when(itemCarritoRepository.findByCarritoIdAndPlatoIdForUpdate(CARRITO_ID, PLATO_ID))
                .thenAnswer(invocation -> Optional.ofNullable(itemCreado.get()));
        when(itemCarritoRepository.saveAndFlush(any(ItemCarrito.class))).thenAnswer(invocation -> {
            ItemCarrito guardado = invocation.getArgument(0);
            guardado.setId(4L);
            itemCreado.set(guardado);
            return guardado;
        });

        carritoService.agregarItem(new AddItemCarritoRequest(PLATO_ID, 1));
        carritoService.agregarItem(new AddItemCarritoRequest(PLATO_ID, 1));

        assertEquals(1, carrito.getItems().size());
        assertEquals(2, itemCreado.get().getCantidad());
    }

    private User cliente() {
        User cliente = new User();
        cliente.setId(CLIENTE_ID);
        cliente.setRole(Role.CLIENTE);
        return cliente;
    }

    private Carrito carrito(User cliente) {
        Carrito carrito = new Carrito();
        carrito.setId(CARRITO_ID);
        carrito.setCliente(cliente);
        return carrito;
    }

    private Plato plato() {
        Plato plato = new Plato();
        plato.setId(PLATO_ID);
        plato.setNombre("Plato");
        plato.setEstado(EstadoPlato.PUBLICADO);
        plato.setStockDisponible(5);
        plato.setPrecio(BigDecimal.TEN);
        plato.setDescuentoPorcentaje(BigDecimal.ZERO);
        return plato;
    }
}
