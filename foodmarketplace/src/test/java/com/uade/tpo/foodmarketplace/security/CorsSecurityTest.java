package com.uade.tpo.foodmarketplace.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.uade.tpo.foodmarketplace.controllers.auth.AuthenticationController;
import com.uade.tpo.foodmarketplace.controllers.common.GlobalExceptionHandler;
import com.uade.tpo.foodmarketplace.controllers.user.UsersController;
import com.uade.tpo.foodmarketplace.entity.dto.user.UserResponse;
import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.exceptions.user.UserNotFoundException;
import com.uade.tpo.foodmarketplace.repository.user.UserRepository;
import com.uade.tpo.foodmarketplace.service.auth.AuthenticationService;
import com.uade.tpo.foodmarketplace.service.user.UserService;

/** Ejecuta la cadena real de seguridad y JWT sin necesitar una base de datos ni Docker. */
@WebMvcTest(controllers = { UsersController.class, AuthenticationController.class }, properties = {
        "JWT_SECRET=" + CorsSecurityTest.JWT_SECRET,
        "CORS_ALLOWED_ORIGINS= http://localhost:5173, , http://localhost:4173, ,"
})
@Import({ SecurityConfig.class, ApplicationConfig.class, JwtAuthenticationFilter.class, JwtService.class,
        RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class, GlobalExceptionHandler.class,
        CorsSecurityTest.TestSecurityConfiguration.class })
class CorsSecurityTest {

    static final String JWT_SECRET = "cors-test-only-secret-key-at-least-32-bytes";
    private static final String ORIGIN = "http://localhost:5173";

    private MockMvc mockMvc;
    @Autowired WebApplicationContext context;
    @Autowired JwtService jwtService;
    @Autowired UrlBasedCorsConfigurationSource corsConfigurationSource;

    @MockitoBean UserRepository userRepository;
    @MockitoBean UserService userService;
    @MockitoBean AuthenticationService authenticationService;

    @BeforeEach
    void configurarMockMvcConLaCadenaReal() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableWebSecurity
    static class TestSecurityConfiguration {
    }

    @ParameterizedTest
    @ValueSource(strings = { "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS" })
    void preflightValidoSinJwtNoLlegaALaAutenticacion(String method) throws Exception {
        mockMvc.perform(options("/users/me")
                        .header(HttpHeaders.ORIGIN, ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, method)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString(method)))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsString("authorization")))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsString("content-type")))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));

        verifyNoInteractions(userRepository, userService, authenticationService);
    }

    @Test
    void propiedadQuitaEspaciosYEntradasVacias() throws Exception {
        var configuration = corsConfigurationSource.getCorsConfiguration(new MockHttpServletRequest("GET", "/users/me"));
        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins()).containsExactly(ORIGIN, "http://localhost:4173");
        assertThat(configuration.getAllowCredentials()).isFalse();

        mockMvc.perform(options("/users/me")
                        .header(HttpHeaders.ORIGIN, "http://localhost:4173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4173"));
    }

    @Test
    void solicitudConJwtValidoConservaCors() throws Exception {
        mockMvc.perform(get("/users/me").header(HttpHeaders.ORIGIN, ORIGIN)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN))
                .andExpect(jsonPath("$.data.username").value("cliente-cors"));

        verify(userRepository).findByUsernameIgnoreCase("cliente-cors");
        verify(userService).getCurrentUser();
    }

    @Test
    void solicitudSinTokenSigueRechazadaYConservaCors() throws Exception {
        mockMvc.perform(get("/users/me").header(HttpHeaders.ORIGIN, ORIGIN))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN))
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(userRepository, userService);
    }

    @Test
    void jwtInvalidoConservaCorsEnElError() throws Exception {
        mockMvc.perform(get("/users/me").header(HttpHeaders.ORIGIN, ORIGIN)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN));

        verifyNoInteractions(userRepository, userService);
    }

    @Test
    void jwtVencidoConservaCorsEnElError() throws Exception {
        User user = cliente();
        String token = new JwtService(JWT_SECRET, -1000).generateToken(user);

        mockMvc.perform(get("/users/me").header(HttpHeaders.ORIGIN, ORIGIN)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN));

        verifyNoInteractions(userRepository, userService);
    }

    @Test
    void clienteSinPermisoConserva403YCors() throws Exception {
        mockMvc.perform(get("/users").header(HttpHeaders.ORIGIN, ORIGIN)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente()))
                .andExpect(status().isForbidden())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN))
                .andExpect(jsonPath("$.success").value(false));

        verify(userRepository).findByUsernameIgnoreCase("cliente-cors");
        verifyNoInteractions(userService);
    }

    @Test
    void errorDeValidacionConservaCors() throws Exception {
        mockMvc.perform(post("/api/v1/auth/authenticate").header(HttpHeaders.ORIGIN, ORIGIN)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN))
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void recursoInexistenteConservaCors() throws Exception {
        String token = tokenCliente();
        when(userService.getCurrentUser()).thenThrow(new UserNotFoundException());

        mockMvc.perform(get("/users/me").header(HttpHeaders.ORIGIN, ORIGIN)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN));
    }

    @Test
    void errorInternoConservaCors() throws Exception {
        String token = tokenCliente();
        when(userService.getCurrentUser()).thenThrow(new IllegalStateException("error de prueba"));

        mockMvc.perform(get("/users/me").header(HttpHeaders.ORIGIN, ORIGIN)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isInternalServerError())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN));
    }

    @Test
    void preflightDeOrigenNoPermitidoEsRechazado() throws Exception {
        mockMvc.perform(options("/users/me")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5174")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));

        verifyNoInteractions(userRepository, userService);
    }

    @Test
    void solicitudRealDeOrigenNoPermitidoEsRechazadaAntesDelJwt() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5174")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));

        verifyNoInteractions(userRepository, userService);
    }

    @Test
    void preflightConMetodoNoPermitidoEsRechazado() throws Exception {
        mockMvc.perform(options("/users/me").header(HttpHeaders.ORIGIN, ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "TRACE"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void preflightConHeaderNoPermitidoEsRechazado() throws Exception {
        mockMvc.perform(options("/users/me").header(HttpHeaders.ORIGIN, ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "x-no-permitido"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void sinOriginConJwtSigueFuncionando() throws Exception {
        mockMvc.perform(get("/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente()))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void sinOriginNiJwtSigueRechazado() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    private String tokenCliente() {
        User user = cliente();
        when(userRepository.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));
        when(userService.getCurrentUser()).thenReturn(new UserResponse(user.getId(), user.getUsername(),
                user.getNombre(), user.getApellido(), user.getEmail(), user.getRole()));
        return jwtService.generateToken(user);
    }

    private User cliente() {
        User user = new User();
        user.setId(1L);
        user.setUsername("cliente-cors");
        user.setNombre("Cliente");
        user.setApellido("CORS");
        user.setEmail("cliente-cors@example.com");
        user.setRole(Role.CLIENTE);
        return user;
    }
}
