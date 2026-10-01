package com.uade.tpo.foodmarketplace.controllers.common;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.uade.tpo.foodmarketplace.controllers.auth.AuthenticationController;
import com.uade.tpo.foodmarketplace.controllers.carrito.CarritoController;
import com.uade.tpo.foodmarketplace.controllers.plato.PlatosController;
import com.uade.tpo.foodmarketplace.controllers.resena.ResenasController;
import com.uade.tpo.foodmarketplace.entity.dto.auth.AuthenticationResponse;
import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.exceptions.user.UserDuplicateException;
import com.uade.tpo.foodmarketplace.service.auth.AuthenticationService;
import com.uade.tpo.foodmarketplace.service.carrito.CarritoService;
import com.uade.tpo.foodmarketplace.service.plato.PlatoService;
import com.uade.tpo.foodmarketplace.service.resena.ResenaService;

class BloqueEHttpContractTest {
    @Test
    void authDevuelveTokenDentroDeApiResponse() throws Exception {
        AuthenticationService service = mock(AuthenticationService.class);
        when(service.authenticate(any())).thenReturn(new AuthenticationResponse("jwt", 7L, Role.CLIENTE));
        var mvc = MockMvcBuilders.standaloneSetup(new AuthenticationController(service)).build();

        mvc.perform(post("/api/v1/auth/authenticate").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"cliente\",\"password\":\"password1\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.data.access_token").value("jwt"))
                .andExpect(jsonPath("$.data.userId").value(7));
    }

    @Test
    void duplicadoDeUsuarioDevuelve400Funcional() throws Exception {
        AuthenticationService service = mock(AuthenticationService.class);
        when(service.register(any())).thenThrow(new UserDuplicateException());
        var mvc = MockMvcBuilders.standaloneSetup(new AuthenticationController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();

        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"cliente\",\"nombre\":\"Ana\",\"apellido\":\"Perez\","
                        + "\"email\":\"ana@mail.com\",\"password\":\"password1\",\"tipoCuenta\":\"CLIENTE\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void eliminacionesDeCarritoPlatoYResenaCompartenApiResponse() throws Exception {
        CarritoController carrito = new CarritoController(mock(CarritoService.class));
        PlatosController platos = new PlatosController();
        ReflectionTestUtils.setField(platos, "platoService", mock(PlatoService.class));
        ResenasController resenas = new ResenasController();
        ReflectionTestUtils.setField(resenas, "resenaService", mock(ResenaService.class));
        var mvc = MockMvcBuilders.standaloneSetup(carrito, platos, resenas).build();

        for (String path : new String[] {"/carrito", "/platos/1", "/resenas/1"}) {
            mvc.perform(delete(path)).andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        }
    }
}
