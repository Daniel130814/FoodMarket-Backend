package com.uade.tpo.foodmarketplace.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.uade.tpo.foodmarketplace.entity.carrito.Carrito;
import com.uade.tpo.foodmarketplace.entity.dto.carrito.AddItemCarritoRequest;
import com.uade.tpo.foodmarketplace.entity.domicilio.Domicilio;
import com.uade.tpo.foodmarketplace.entity.order.DetallePedido;
import com.uade.tpo.foodmarketplace.entity.order.EstadoPedido;
import com.uade.tpo.foodmarketplace.entity.order.Order;
import com.uade.tpo.foodmarketplace.entity.order.SubPedidoChef;
import com.uade.tpo.foodmarketplace.entity.pago.EstadoPago;
import com.uade.tpo.foodmarketplace.entity.pago.MedioPago;
import com.uade.tpo.foodmarketplace.entity.pago.Pago;
import com.uade.tpo.foodmarketplace.entity.plato.EstadoPlato;
import com.uade.tpo.foodmarketplace.entity.plato.Plato;
import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.exceptions.common.BusinessRuleException;
import com.uade.tpo.foodmarketplace.exceptions.order.InvalidOrderStateException;
import com.uade.tpo.foodmarketplace.exceptions.pago.InvalidPagoStateException;
import com.uade.tpo.foodmarketplace.repository.carrito.CarritoRepository;
import com.uade.tpo.foodmarketplace.repository.carrito.ItemCarritoRepository;
import com.uade.tpo.foodmarketplace.repository.domicilio.DomicilioRepository;
import com.uade.tpo.foodmarketplace.repository.order.OrderRepository;
import com.uade.tpo.foodmarketplace.repository.order.SubPedidoChefRepository;
import com.uade.tpo.foodmarketplace.repository.pago.PagoRepository;
import com.uade.tpo.foodmarketplace.repository.plato.PlatoRepository;
import com.uade.tpo.foodmarketplace.repository.user.UserRepository;
import com.uade.tpo.foodmarketplace.security.AuthenticatedUserService;
import com.uade.tpo.foodmarketplace.service.carrito.CarritoService;
import com.uade.tpo.foodmarketplace.service.order.OrderService;
import com.uade.tpo.foodmarketplace.service.order.SubPedidoChefService;
import com.uade.tpo.foodmarketplace.service.pago.PagoService;

/** Each worker invokes a proxied service in its own real transaction; the test itself is not transactional. */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class RobustezConcurrencyMySqlTest extends MySqlIntegrationTestBase {
    @Autowired UserRepository users;
    @Autowired DomicilioRepository domicilios;
    @Autowired PlatoRepository platos;
    @Autowired OrderRepository orders;
    @Autowired SubPedidoChefRepository subPedidos;
    @Autowired PagoRepository pagos;
    @Autowired CarritoRepository carritos;
    @Autowired ItemCarritoRepository items;
    @Autowired OrderService orderService;
    @Autowired PagoService pagoService;
    @Autowired SubPedidoChefService subPedidoService;
    @Autowired CarritoService carritoService;
    @MockitoBean AuthenticatedUserService auth;

    @Test
    void dosAprobacionesSoloPersistenUna() throws Exception {
        User cliente = user(Role.CLIENTE);
        Order order = order(cliente, EstadoPedido.PENDIENTE);
        Long a = pago(order, EstadoPago.PENDIENTE).getId();
        Long b = pago(order, EstadoPago.PENDIENTE).getId();
        Race result = race(() -> pagoService.actualizarEstadoPago(a, EstadoPago.APROBADO),
                () -> pagoService.actualizarEstadoPago(b, EstadoPago.APROBADO));
        assertEquals(1, result.successes());
        assertEquals(1, result.failuresOf(InvalidPagoStateException.class));
        assertEquals(1, pagos.countByPedidoIdAndEstado(order.getId(), EstadoPago.APROBADO));
    }

    @Test
    void dobleCancelacionReponeStockUnaSolaVez() throws Exception {
        User cliente = user(Role.CLIENTE);
        when(auth.getCurrentUser()).thenReturn(cliente);
        User chef = user(Role.CHEF);
        Plato plato = plato(chef, 8);
        Order order = order(cliente, EstadoPedido.PENDIENTE);
        SubPedidoChef sub = subPedido(order, chef, EstadoPedido.PENDIENTE);
        DetallePedido detalle = new DetallePedido();
        detalle.setSubPedidoChef(sub);
        detalle.setPlato(plato);
        detalle.setCantidad(2);
        detalle.setPrecioUnitario(BigDecimal.TEN);
        detalle.setSubtotal(BigDecimal.valueOf(20));
        sub.getDetalles().add(detalle);
        subPedidos.saveAndFlush(sub);
        Race result = race(() -> orderService.cancelarOrder(order.getId()),
                () -> orderService.cancelarOrder(order.getId()));
        assertEquals(1, result.successes());
        assertEquals(1, result.failuresOf(InvalidOrderStateException.class));
        assertEquals(EstadoPedido.CANCELADO, orders.findById(order.getId()).orElseThrow().getEstado());
        assertEquals(10, platos.findById(plato.getId()).orElseThrow().getStockDisponible());
    }

    @Test
    void dosChefsConservanEstadosYRecalculo() throws Exception {
        User cliente = user(Role.CLIENTE);
        User chefA = user(Role.CHEF);
        User chefB = user(Role.CHEF);
        when(auth.getCurrentUser()).thenReturn(cliente);
        Order order = order(cliente, EstadoPedido.EN_PREPARACION);
        Long a = subPedido(order, chefA, EstadoPedido.EN_PREPARACION).getId();
        Long b = subPedido(order, chefB, EstadoPedido.CONFIRMADO).getId();
        Race result = race(() -> subPedidoService.actualizarEstado(a, EstadoPedido.ENVIADO),
                () -> subPedidoService.actualizarEstado(b, EstadoPedido.EN_PREPARACION));
        assertEquals(2, result.successes(), result.toString());
        assertEquals(EstadoPedido.ENVIADO, subPedidos.findById(a).orElseThrow().getEstado());
        assertEquals(EstadoPedido.EN_PREPARACION, subPedidos.findById(b).orElseThrow().getEstado());
        assertEquals(EstadoPedido.ENVIADO, orders.findById(order.getId()).orElseThrow().getEstado());
    }

    @Test
    void quintoRechazoBloqueaSexto() throws Exception {
        Order order = order(user(Role.CLIENTE), EstadoPedido.PENDIENTE);
        for (int i = 0; i < 4; i++) pago(order, EstadoPago.RECHAZADO);
        Long a = pago(order, EstadoPago.PENDIENTE).getId();
        Long b = pago(order, EstadoPago.PENDIENTE).getId();
        Race result = race(() -> pagoService.actualizarEstadoPago(a, EstadoPago.RECHAZADO),
                () -> pagoService.actualizarEstadoPago(b, EstadoPago.RECHAZADO));
        assertEquals(1, result.successes());
        assertEquals(1, result.failuresOf(BusinessRuleException.class));
        assertEquals(5, pagos.countByPedidoIdAndEstado(order.getId(), EstadoPago.RECHAZADO));
        assertTrue(orders.findById(order.getId()).orElseThrow().isPagoBloqueado());
    }

    @Test
    void primerCarritoSeCreaUnaVez() throws Exception {
        User cliente = user(Role.CLIENTE);
        when(auth.getCurrentUser()).thenReturn(cliente);
        Race result = race(carritoService::getMiCarrito, carritoService::getMiCarrito);
        assertEquals(2, result.successes(), result.toString());
        assertEquals(1, carritos.findAll().stream().filter(c -> c.getCliente().getId().equals(cliente.getId())).count());
    }

    @Test
    void mismoItemAcumulaAmbosIncrementos() throws Exception {
        User cliente = user(Role.CLIENTE);
        when(auth.getCurrentUser()).thenReturn(cliente);
        Plato plato = plato(user(Role.CHEF), 10);
        carritoService.agregarItem(new AddItemCarritoRequest(plato.getId(), 1));
        Carrito carrito = carritos.findByClienteId(cliente.getId()).orElseThrow();
        Race result = race(() -> carritoService.agregarItem(new AddItemCarritoRequest(plato.getId(), 1)),
                () -> carritoService.agregarItem(new AddItemCarritoRequest(plato.getId(), 1)));
        assertEquals(2, result.successes(), result.toString());
        assertEquals(3, items.findByCarritoIdAndPlatoId(carrito.getId(), plato.getId()).orElseThrow().getCantidad());
    }

    private User user(Role role) {
        String id = UUID.randomUUID().toString().substring(0, 12);
        User user = new User();
        user.setUsername("u" + id);
        user.setEmail(id + "@test.invalid");
        user.setPassword("test-password");
        user.setRole(role);
        return users.saveAndFlush(user);
    }

    private Order order(User cliente, EstadoPedido estado) {
        Domicilio domicilio = new Domicilio();
        domicilio.setUsuario(cliente);
        domicilio.setCalle("Test");
        domicilio.setNumero("1");
        domicilio.setCiudad("Test");
        domicilio.setProvincia("Test");
        domicilio.setCodigoPostal("1000");
        domicilios.saveAndFlush(domicilio);
        Order order = new Order();
        order.setUser(cliente);
        order.setDomicilioEntrega(domicilio);
        order.setPrecioFinal(BigDecimal.valueOf(20));
        order.setFechaCreacion(LocalDateTime.now());
        order.setEstado(estado);
        return orders.saveAndFlush(order);
    }

    private Plato plato(User chef, int stock) {
        Plato plato = new Plato();
        plato.setChef(chef);
        plato.setNombre("Plato test");
        plato.setEstado(EstadoPlato.PUBLICADO);
        plato.setPrecio(BigDecimal.TEN);
        plato.setStockDisponible(stock);
        return platos.saveAndFlush(plato);
    }

    private SubPedidoChef subPedido(Order order, User chef, EstadoPedido estado) {
        SubPedidoChef sub = new SubPedidoChef();
        sub.setPedido(order);
        sub.setChef(chef);
        sub.setEstado(estado);
        sub.setSubtotal(BigDecimal.valueOf(20));
        return subPedidos.saveAndFlush(sub);
    }

    private Pago pago(Order order, EstadoPago estado) {
        Pago pago = new Pago();
        pago.setPedido(order);
        pago.setMonto(BigDecimal.valueOf(20));
        pago.setFechaCreacion(LocalDateTime.now());
        pago.setEstado(estado);
        pago.setMedioPago(MedioPago.EFECTIVO);
        return pagos.saveAndFlush(pago);
    }

    private Race race(Callable<?> first, Callable<?> second) throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Throwable> a = executor.submit(() -> attempt(first, ready, start));
            Future<Throwable> b = executor.submit(() -> attempt(second, ready, start));
            assertTrue(ready.await(5, TimeUnit.SECONDS), "workers did not become ready");
            start.countDown();
            return new Race(a.get(20, TimeUnit.SECONDS), b.get(20, TimeUnit.SECONDS));
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "workers did not terminate");
        }
    }

    private Throwable attempt(Callable<?> action, CountDownLatch ready, CountDownLatch start) {
        try {
            ready.countDown();
            if (!start.await(5, TimeUnit.SECONDS)) throw new AssertionError("start timed out");
            action.call();
            return null;
        } catch (Throwable failure) {
            return failure;
        }
    }

    private record Race(Throwable first, Throwable second) {
        int successes() { return (first == null ? 1 : 0) + (second == null ? 1 : 0); }
        int failuresOf(Class<? extends Throwable> type) {
            return (type.isInstance(first) ? 1 : 0) + (type.isInstance(second) ? 1 : 0);
        }
    }
}
