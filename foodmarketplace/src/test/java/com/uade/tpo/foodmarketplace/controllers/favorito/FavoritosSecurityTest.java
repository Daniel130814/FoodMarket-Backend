package com.uade.tpo.foodmarketplace.controllers.favorito;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** Reglas HTTP de /favoritos: solo CLIENTE (401 sin token, 403 para CHEF y ADMIN). */
@org.springframework.test.context.ActiveProfiles("test")
@org.springframework.boot.test.context.SpringBootTest
@org.testcontainers.junit.jupiter.Testcontainers(disabledWithoutDocker = true)
@AutoConfigureMockMvc
class FavoritosSecurityTest extends com.uade.tpo.foodmarketplace.integration.MySqlIntegrationTestBase {

    @Autowired
    MockMvc mockMvc;

    @Test
    void sinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/favoritos")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/favoritos/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/favoritos/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void chefYAdminRecibenForbidden() throws Exception {
        for (String rol : new String[] { "CHEF", "ADMIN" }) {
            mockMvc.perform(get("/favoritos").with(user("u").authorities(() -> rol)))
                    .andExpect(status().isForbidden());
            mockMvc.perform(post("/favoritos/1").with(user("u").authorities(() -> rol)))
                    .andExpect(status().isForbidden());
            mockMvc.perform(delete("/favoritos/1").with(user("u").authorities(() -> rol)))
                    .andExpect(status().isForbidden());
        }
    }
}
