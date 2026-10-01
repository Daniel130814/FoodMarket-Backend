package com.uade.tpo.foodmarketplace.controllers.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.uade.tpo.foodmarketplace.entity.dto.user.UserResponse;
import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.exceptions.user.UserNotFoundException;
import com.uade.tpo.foodmarketplace.service.user.UserService;

@ExtendWith(MockitoExtension.class)
class UsersControllerTest {
    @Mock UserService service;
    @InjectMocks UsersController controller;

    @Test void listaVaciaDevuelveDataNull() {
        when(service.getUsers()).thenReturn(List.of());
        var response = controller.getUsers();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().success());
        assertEquals("No hay usuarios registrados", response.getBody().message());
        assertNull(response.getBody().data());
    }

    @Test void listaConDatosYMeUsanUserResponse() {
        UserResponse user = new UserResponse(4L, "ana", "Ana", "Paz", "ana@example.com", Role.CLIENTE);
        when(service.getUsers()).thenReturn(List.of(user));
        when(service.getCurrentUser()).thenReturn(user);
        assertEquals(user, controller.getUsers().getBody().data().getFirst());
        assertEquals(user, controller.me().getBody().data());
    }

    @Test void usuarioInexistenteLanzaNotFoundDelDominio() {
        when(service.getUserById(99L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> controller.getUserById(99L));
    }
}
