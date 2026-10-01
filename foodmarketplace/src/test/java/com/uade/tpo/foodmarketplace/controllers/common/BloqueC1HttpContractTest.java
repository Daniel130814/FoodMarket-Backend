package com.uade.tpo.foodmarketplace.controllers.common;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.util.ReflectionTestUtils;

import com.uade.tpo.foodmarketplace.controllers.chefprofile.ChefProfilesController;
import com.uade.tpo.foodmarketplace.controllers.order.SubPedidosChefController;
import com.uade.tpo.foodmarketplace.controllers.user.UsersController;
import com.uade.tpo.foodmarketplace.service.chefprofile.ChefProfileService;
import com.uade.tpo.foodmarketplace.service.order.SubPedidoChefService;
import com.uade.tpo.foodmarketplace.service.user.UserService;

class BloqueC1HttpContractTest {

    @Test void listasVaciasIncluyenDataNullEnJson() throws Exception {
        ChefProfileService chefs = org.mockito.Mockito.mock(ChefProfileService.class);
        UserService users = org.mockito.Mockito.mock(UserService.class);
        SubPedidoChefService subPedidos = org.mockito.Mockito.mock(SubPedidoChefService.class);
        UsersController usersController = new UsersController();
        ReflectionTestUtils.setField(usersController, "userService", users);
        when(chefs.getChefProfiles()).thenReturn(List.of());
        when(users.getUsers()).thenReturn(List.of());
        when(subPedidos.getSubPedidosByOrderId(1L)).thenReturn(List.of());
        when(subPedidos.getSubPedidosByChefId(2L)).thenReturn(List.of());
        var mvc = MockMvcBuilders.standaloneSetup(new ChefProfilesController(chefs), usersController,
                new SubPedidosChefController(subPedidos)).setControllerAdvice(new GlobalExceptionHandler()).build();

        for (String path : List.of("/chef-profiles", "/users", "/subpedidos/order/1", "/subpedidos/chef/2")) {
            mvc.perform(get(path)).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("\"data\":null")));
        }
    }

    @Test void idsInexistentesDevuelven404ConBody() throws Exception {
        ChefProfileService chefs = org.mockito.Mockito.mock(ChefProfileService.class);
        UserService users = org.mockito.Mockito.mock(UserService.class);
        SubPedidoChefService subPedidos = org.mockito.Mockito.mock(SubPedidoChefService.class);
        UsersController usersController = new UsersController();
        ReflectionTestUtils.setField(usersController, "userService", users);
        when(chefs.getChefProfileById(99L)).thenReturn(Optional.empty());
        when(users.getUserById(99L)).thenReturn(Optional.empty());
        when(subPedidos.getSubPedidoById(99L)).thenReturn(Optional.empty());
        var mvc = MockMvcBuilders.standaloneSetup(new ChefProfilesController(chefs), usersController,
                new SubPedidosChefController(subPedidos)).setControllerAdvice(new GlobalExceptionHandler()).build();

        for (String path : List.of("/chef-profiles/99", "/users/99", "/subpedidos/99")) {
            mvc.perform(get(path)).andExpect(status().isNotFound()).andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("\"data\":null")));
        }
    }
}
