package com.uade.tpo.foodmarketplace.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Comprueba la frontera HTTP: 401 identifica falta de autenticacion y 403 falta de permisos. */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired MockMvc mockMvc;

    @Test
    void catalogoEsPublico() throws Exception {
        mockMvc.perform(get("/platos")).andExpect(status().isOk());
    }

    @Test
    void endpointProtegidoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/users/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenInvalidoDevuelve401() throws Exception {
        mockMvc.perform(get("/users/me").header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registroAdminPublicoEsRechazado() throws Exception {
        String body = """
                {"nombre":"Admin","apellido":"Local","email":"admin@mail.com",
                 "password":"password1","tipoCuenta":"ADMIN"}
                """;
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void clienteNoPuedeCrearPlato() throws Exception {
        mockMvc.perform(post("/platos/createPlato").with(user("cliente").authorities(() -> "CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void chefSuperaAutorizacionParaCrearPlato() throws Exception {
        mockMvc.perform(post("/platos/createPlato").with(user("chef").authorities(() -> "CHEF"))
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void clienteSuperaAutorizacionParaCrearOrder() throws Exception {
        mockMvc.perform(post("/orders/createOrder").with(user("cliente").authorities(() -> "CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void chefNoPuedeCrearOrder() throws Exception {
        mockMvc.perform(post("/orders/createOrder").with(user("chef").authorities(() -> "CHEF"))
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void clienteNoPuedeAdministrarCategoria() throws Exception {
        mockMvc.perform(post("/categories/createCategory").with(user("cliente").authorities(() -> "CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminSuperaAutorizacionParaAdministrarCategoria() throws Exception {
        mockMvc.perform(post("/categories/createCategory").with(user("admin").authorities(() -> "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}
