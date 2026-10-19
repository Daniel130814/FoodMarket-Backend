package com.uade.tpo.foodmarketplace.security;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.uade.tpo.foodmarketplace.controllers.auth.AuthenticationController;
import com.uade.tpo.foodmarketplace.controllers.common.GlobalExceptionHandler;
import com.uade.tpo.foodmarketplace.controllers.user.UsersController;
import com.uade.tpo.foodmarketplace.entity.order.EstadoPedido;
import com.uade.tpo.foodmarketplace.entity.plato.EstadoPlato;
import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.repository.order.SubPedidoChefRepository;
import com.uade.tpo.foodmarketplace.repository.plato.PlatoRepository;
import com.uade.tpo.foodmarketplace.repository.user.UserRepository;
import com.uade.tpo.foodmarketplace.service.auth.AuthenticationService;
import com.uade.tpo.foodmarketplace.service.user.UserServiceImpl;

/** Contrato HTTP con controlador, servicios, JWT y seguridad reales; solo la persistencia es simulada. */
@WebMvcTest(controllers = { UsersController.class, AuthenticationController.class }, properties = {
        "JWT_SECRET=user-role-test-only-secret-key-at-least-32-bytes"
})
@Import({ SecurityConfig.class, ApplicationConfig.class, JwtAuthenticationFilter.class, JwtService.class,
        RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class, GlobalExceptionHandler.class,
        UserServiceImpl.class, AuthenticatedUserService.class, AuthenticationService.class,
        UserRoleSecurityTest.TestSecurityConfiguration.class })
class UserRoleSecurityTest {
    @Autowired WebApplicationContext context;
    @Autowired JwtService jwtService;
    @Autowired PasswordEncoder passwordEncoder;
    @MockitoBean UserRepository userRepository;
    @MockitoBean PlatoRepository platoRepository;
    @MockitoBean SubPedidoChefRepository subPedidoChefRepository;

    private MockMvc mockMvc;
    private User admin;
    private User target;
    private String adminToken;

    @TestConfiguration(proxyBeanMethods = false)
    @EnableWebSecurity
    static class TestSecurityConfiguration {
    }

    @BeforeEach
    void configurarCadenaReal() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        admin = usuario(1L, Role.ADMIN);
        target = usuario(2L, Role.CLIENTE);
        when(userRepository.findByUsernameIgnoreCase(admin.getUsername())).thenReturn(Optional.of(admin));
        when(userRepository.findByUsernameIgnoreCase(target.getUsername())).thenReturn(Optional.of(target));
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(call -> call.getArgument(0));
        adminToken = jwtService.generateToken(admin);
    }

    @Test
    void adminCambiaRolConRespuestaSegura() throws Exception {
        cambiar(adminToken, 2L, "{\"role\":\"CHEF\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Rol del usuario actualizado correctamente"))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.username").value("usuario2"))
                .andExpect(jsonPath("$.data.role").value("CHEF"))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.access_token").doesNotExist());
        verify(userRepository).save(target);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = { "CLIENTE", "CHEF" })
    void otrosRolesNoPuedenCambiarRoles(Role role) throws Exception {
        target.setRole(role);
        cambiar(jwtService.generateToken(target), 1L, "{\"role\":\"CLIENTE\"}")
                .andExpect(status().isForbidden());
        verify(userRepository, never()).findByIdForUpdate(anyLong());
        verify(userRepository, never()).save(any());
    }

    @Test
    void sinAutenticacionDevuelve401() throws Exception {
        mockMvc.perform(patch("/users/2/role").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isUnauthorized());
        verify(userRepository, never()).save(any());
    }

    @Test
    void jwtInvalidoOVencidoDevuelve401() throws Exception {
        cambiar("token-invalido", 2L, "{\"role\":\"ADMIN\"}").andExpect(status().isUnauthorized());
        String expired = new JwtService("user-role-test-only-secret-key-at-least-32-bytes", -1000).generateToken(admin);
        cambiar(expired, 2L, "{\"role\":\"ADMIN\"}").andExpect(status().isUnauthorized());
        verify(userRepository, never()).save(any());
    }

    @Test
    void usuarioInexistenteDevuelve404() throws Exception {
        cambiar(adminToken, 99L, "{\"role\":\"ADMIN\"}")
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.success").value(false));
        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = { "{}", "{\"role\":null}", "{\"role\":\"OTRO\"}", "{", "{\"role\":\"\"}" })
    void bodyInvalidoDevuelve400SinEscrituras(String body) throws Exception {
        cambiar(adminToken, 2L, body).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        verify(userRepository, never()).findByIdForUpdate(anyLong());
        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void repetirRolDevuelve200SinGuardar(Role role) throws Exception {
        target.setRole(role);
        cambiar(adminToken, 2L, "{\"role\":\"" + role + "\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.role").value(role.name()));
        verify(userRepository, never()).save(any());
        verifyNoInteractions(platoRepository, subPedidoChefRepository);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = { "CLIENTE", "CHEF" })
    void autodegradacionDevuelve400(Role role) throws Exception {
        cambiar(adminToken, 1L, "{\"role\":\"" + role + "\"}").andExpect(status().isBadRequest());
        verify(userRepository, never()).save(any());
    }

    @Test
    void administradorRepiteSuPropioRol() throws Exception {
        cambiar(adminToken, 1L, "{\"role\":\"ADMIN\"}").andExpect(status().isOk());
        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest
    @CsvSource({ "PUBLICADO,CLIENTE", "AGOTADO,CLIENTE", "PUBLICADO,ADMIN", "AGOTADO,ADMIN" })
    void platoComprableDevuelve409(EstadoPlato estado, Role nuevo) throws Exception {
        target.setRole(Role.CHEF);
        when(platoRepository.existsByChefIdAndEstadoIn(eq(2L), anyList()))
                .thenAnswer(call -> call.<List<EstadoPlato>>getArgument(1).contains(estado));
        cambiar(adminToken, 2L, "{\"role\":\"" + nuevo + "\"}").andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("No se puede quitar el rol CHEF")))
                .andExpect(jsonPath("$.data").isEmpty());
        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest
    @CsvSource({ "PENDIENTE,CLIENTE", "CONFIRMADO,CLIENTE", "EN_PREPARACION,CLIENTE", "ENVIADO,CLIENTE",
            "PENDIENTE,ADMIN", "CONFIRMADO,ADMIN", "EN_PREPARACION,ADMIN", "ENVIADO,ADMIN" })
    void subpedidoSinTerminarDevuelve409(EstadoPedido estado, Role nuevo) throws Exception {
        target.setRole(Role.CHEF);
        when(subPedidoChefRepository.existsByChefIdAndEstadoIn(eq(2L), anyList()))
                .thenAnswer(call -> call.<List<EstadoPedido>>getArgument(1).contains(estado));
        cambiar(adminToken, 2L, "{\"role\":\"" + nuevo + "\"}").andExpect(status().isConflict());
        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest
    @CsvSource({ "BORRADOR,ENTREGADO,CLIENTE", "PAUSADO,CANCELADO,CLIENTE",
            "BORRADOR,CANCELADO,ADMIN", "PAUSADO,ENTREGADO,ADMIN" })
    void historialTerminadoYPlatosNoComprablesPermitenCambio(EstadoPlato plato, EstadoPedido pedido, Role nuevo)
            throws Exception {
        target.setRole(Role.CHEF);
        when(platoRepository.existsByChefIdAndEstadoIn(eq(2L), anyList()))
                .thenAnswer(call -> call.<List<EstadoPlato>>getArgument(1).contains(plato));
        when(subPedidoChefRepository.existsByChefIdAndEstadoIn(eq(2L), anyList()))
                .thenAnswer(call -> call.<List<EstadoPedido>>getArgument(1).contains(pedido));
        cambiar(adminToken, 2L, "{\"role\":\"" + nuevo + "\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.role").value(nuevo.name()));
    }

    @Test
    void mismoJwtAdquiereYPierdePermisosEnLaSiguienteSolicitud() throws Exception {
        String existingToken = jwtService.generateToken(target);
        mockMvc.perform(get("/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + existingToken))
                .andExpect(status().isForbidden());
        cambiar(adminToken, 2L, "{\"role\":\"ADMIN\"}").andExpect(status().isOk());
        mockMvc.perform(get("/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + existingToken))
                .andExpect(status().isOk());
        cambiar(adminToken, 2L, "{\"role\":\"CLIENTE\"}").andExpect(status().isOk());
        mockMvc.perform(get("/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + existingToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void preflightPatchSiguePermitidoSinToken() throws Exception {
        mockMvc.perform(options("/users/2/role").header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PATCH")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("PATCH")));
        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = { "CLIENTE", "CHEF" })
    void registroPublicoConservaSoloLosRolesPermitidos(Role role) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                {"username":"nueva","nombre":"Ana","apellido":"Paz","email":"nueva@example.com",
                 "password":"Password123!","tipoCuenta":"%s","role":"ADMIN"}
                """.formatted(role)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.role").value(role.name()))
                .andExpect(jsonPath("$.data.access_token").isNotEmpty());
    }

    @Test
    void registroPublicoNoPermiteAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                {"username":"nueva","nombre":"Ana","apellido":"Paz","email":"nueva@example.com",
                 "password":"Password123!","tipoCuenta":"ADMIN"}
                """))
                .andExpect(status().isBadRequest());
        verify(userRepository, never()).save(any());
    }

    @Test
    void edicionPropiaNoCambiaRolNiContrasena() throws Exception {
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        mockMvc.perform(put("/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.generateToken(target))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                        {"nombre":"Ana editada","apellido":"Paz","email":"editada@example.com",
                         "role":"ADMIN","password":"otro-password"}
                        """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.role").value("CLIENTE"));
        org.junit.jupiter.api.Assertions.assertEquals("hash-original", target.getPassword());
        verify(userRepository).saveAndFlush(target);
    }

    @Test
    void loginPublicoSigueEntregandoToken() throws Exception {
        target.setPassword(passwordEncoder.encode("Password123!"));
        mockMvc.perform(post("/api/v1/auth/authenticate").contentType(MediaType.APPLICATION_JSON).content("""
                {"username":"usuario2","password":"Password123!"}
                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.access_token").isNotEmpty())
                .andExpect(jsonPath("$.data.role").value("CLIENTE"));
    }

    private org.springframework.test.web.servlet.ResultActions cambiar(String token, Long id, String body) throws Exception {
        return mockMvc.perform(patch("/users/{userId}/role", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private User usuario(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername("usuario" + id);
        user.setNombre("Ana");
        user.setApellido("Paz");
        user.setEmail("usuario" + id + "@example.com");
        user.setPassword("hash-original");
        user.setRole(role);
        return user;
    }
}
