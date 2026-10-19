package com.uade.tpo.foodmarketplace.service.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.uade.tpo.foodmarketplace.entity.chefprofile.ChefProfile;
import com.uade.tpo.foodmarketplace.entity.domicilio.Domicilio;
import com.uade.tpo.foodmarketplace.entity.order.EstadoPedido;
import com.uade.tpo.foodmarketplace.entity.order.Order;
import com.uade.tpo.foodmarketplace.entity.plato.EstadoPlato;
import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.exceptions.common.BusinessRuleException;
import com.uade.tpo.foodmarketplace.exceptions.common.ResourceInUseException;
import com.uade.tpo.foodmarketplace.exceptions.user.UserNotFoundException;
import com.uade.tpo.foodmarketplace.repository.order.SubPedidoChefRepository;
import com.uade.tpo.foodmarketplace.repository.plato.PlatoRepository;
import com.uade.tpo.foodmarketplace.repository.user.UserRepository;
import com.uade.tpo.foodmarketplace.security.AuthenticatedUserService;

/** Comprueba reglas y ausencia de escrituras cuando el cambio se rechaza o se repite. */
@ExtendWith(MockitoExtension.class)
class UserRoleServiceTest {
    @Mock UserRepository userRepository;
    @Mock PlatoRepository platoRepository;
    @Mock SubPedidoChefRepository subPedidoChefRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticatedUserService authenticatedUserService;
    @InjectMocks UserServiceImpl service;

    @BeforeEach
    void administradorActual() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(usuario(1L, Role.ADMIN));
    }

    @AfterEach
    void nuncaSeProcesaLaContrasena() {
        verifyNoInteractions(passwordEncoder);
    }

    @ParameterizedTest
    @CsvSource({ "CLIENTE,CHEF", "CLIENTE,ADMIN", "CHEF,CLIENTE", "CHEF,ADMIN", "ADMIN,CLIENTE", "ADMIN,CHEF" })
    void cambiaSoloElRolDeOtroUsuario(Role anterior, Role nuevo) {
        User user = destinatario(anterior);
        ChefProfile profile = new ChefProfile();
        profile.setUser(user);
        user.setChefProfile(profile);
        List<Order> pedidos = List.of(new Order());
        List<Domicilio> domicilios = List.of(new Domicilio());
        user.setOrders(pedidos);
        user.setDomicilios(domicilios);
        when(userRepository.save(user)).thenReturn(user);

        var response = service.updateRole(2L, nuevo);

        assertEquals(nuevo, response.role());
        assertEquals(nuevo, user.getRole());
        assertEquals(2L, user.getId());
        assertEquals("usuario2", user.getUsername());
        assertEquals("Ana", user.getNombre());
        assertEquals("Paz", user.getApellido());
        assertEquals("ana@example.com", user.getEmail());
        assertEquals("hash-que-debe-conservarse", user.getPassword());
        assertSame(profile, user.getChefProfile());
        assertSame(pedidos, user.getOrders());
        assertSame(domicilios, user.getDomicilios());
        verify(userRepository).save(user);
        verify(platoRepository, never()).save(any());
        verify(subPedidoChefRepository, never()).save(any());
        if (anterior != Role.CHEF) {
            verifyNoInteractions(platoRepository, subPedidoChefRepository);
        }
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void repetirRolNoGuardaNiConsultaActividad(Role role) {
        User user = destinatario(role);
        assertEquals(role, service.updateRole(2L, role).role());
        assertEquals(role, user.getRole());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(platoRepository, subPedidoChefRepository);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = { "CLIENTE", "CHEF" })
    void servicioTambienRechazaSolicitantesSinPermisos(Role role) {
        when(authenticatedUserService.getCurrentUser()).thenReturn(usuario(1L, role));
        assertThrows(AccessDeniedException.class, () -> service.updateRole(2L, Role.ADMIN));
        verifyNoInteractions(userRepository, platoRepository, subPedidoChefRepository);
    }

    @Test
    void destinatarioInexistenteNoGuarda() {
        when(userRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> service.updateRole(99L, Role.ADMIN));
        verify(userRepository, never()).save(any());
        verifyNoInteractions(platoRepository, subPedidoChefRepository);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = { "CLIENTE", "CHEF" })
    void administradorNoPuedeQuitarseSuRol(Role role) {
        User admin = usuario(1L, Role.ADMIN);
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(admin));
        assertThrows(BusinessRuleException.class, () -> service.updateRole(1L, role));
        assertEquals(Role.ADMIN, admin.getRole());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(platoRepository, subPedidoChefRepository);
    }

    @Test
    void administradorPuedeRepetirSuPropioRolSinGuardar() {
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(usuario(1L, Role.ADMIN)));
        assertEquals(Role.ADMIN, service.updateRole(1L, Role.ADMIN).role());
        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest
    @CsvSource({ "PUBLICADO,CLIENTE", "AGOTADO,CLIENTE", "PUBLICADO,ADMIN", "AGOTADO,ADMIN" })
    void platosComprablesBloqueanLaSalida(EstadoPlato estado, Role nuevo) {
        User chef = destinatario(Role.CHEF);
        when(platoRepository.existsByChefIdAndEstadoIn(eq(2L), anyList()))
                .thenAnswer(call -> call.<List<EstadoPlato>>getArgument(1).contains(estado));
        assertThrows(ResourceInUseException.class, () -> service.updateRole(2L, nuevo));
        assertEquals(Role.CHEF, chef.getRole());
        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest
    @CsvSource({ "PENDIENTE,CLIENTE", "CONFIRMADO,CLIENTE", "EN_PREPARACION,CLIENTE", "ENVIADO,CLIENTE",
            "PENDIENTE,ADMIN", "CONFIRMADO,ADMIN", "EN_PREPARACION,ADMIN", "ENVIADO,ADMIN" })
    void subpedidosSinTerminarBloqueanLaSalida(EstadoPedido estado, Role nuevo) {
        User chef = destinatario(Role.CHEF);
        when(subPedidoChefRepository.existsByChefIdAndEstadoIn(eq(2L), anyList()))
                .thenAnswer(call -> call.<List<EstadoPedido>>getArgument(1).contains(estado));
        assertThrows(ResourceInUseException.class, () -> service.updateRole(2L, nuevo));
        assertEquals(Role.CHEF, chef.getRole());
        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest
    @CsvSource({ "BORRADOR,ENTREGADO,CLIENTE", "PAUSADO,CANCELADO,CLIENTE",
            "BORRADOR,CANCELADO,ADMIN", "PAUSADO,ENTREGADO,ADMIN" })
    void publicacionesNoComprablesEHistorialTerminadoPermitenSalir(EstadoPlato plato, EstadoPedido pedido, Role nuevo) {
        User chef = destinatario(Role.CHEF);
        when(platoRepository.existsByChefIdAndEstadoIn(eq(2L), anyList()))
                .thenAnswer(call -> call.<List<EstadoPlato>>getArgument(1).contains(plato));
        when(subPedidoChefRepository.existsByChefIdAndEstadoIn(eq(2L), anyList()))
                .thenAnswer(call -> call.<List<EstadoPedido>>getArgument(1).contains(pedido));
        when(userRepository.save(chef)).thenReturn(chef);
        assertEquals(nuevo, service.updateRole(2L, nuevo).role());
        verify(platoRepository).existsByChefIdAndEstadoIn(2L, List.of(EstadoPlato.PUBLICADO, EstadoPlato.AGOTADO));
        verify(subPedidoChefRepository).existsByChefIdAndEstadoIn(2L,
                List.of(EstadoPedido.PENDIENTE, EstadoPedido.CONFIRMADO, EstadoPedido.EN_PREPARACION, EstadoPedido.ENVIADO));
    }

    @Test
    void entrarAChefNoExigeNiCreaPerfilYVolverConservaElExistente() {
        User user = destinatario(Role.CLIENTE);
        when(userRepository.save(user)).thenReturn(user);
        assertEquals(Role.CHEF, service.updateRole(2L, Role.CHEF).role());
        assertNull(user.getChefProfile());
        ChefProfile profile = new ChefProfile();
        user.setChefProfile(profile);
        assertEquals(Role.CLIENTE, service.updateRole(2L, Role.CLIENTE).role());
        assertSame(profile, user.getChefProfile());
        assertEquals(Role.CHEF, service.updateRole(2L, Role.CHEF).role());
        assertSame(profile, user.getChefProfile());
        assertEquals("hash-que-debe-conservarse", user.getPassword());
    }

    private User destinatario(Role role) {
        User user = usuario(2L, role);
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(user));
        return user;
    }

    private User usuario(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername("usuario" + id);
        user.setNombre("Ana");
        user.setApellido("Paz");
        user.setEmail("ana@example.com");
        user.setPassword("hash-que-debe-conservarse");
        user.setRole(role);
        return user;
    }
}
