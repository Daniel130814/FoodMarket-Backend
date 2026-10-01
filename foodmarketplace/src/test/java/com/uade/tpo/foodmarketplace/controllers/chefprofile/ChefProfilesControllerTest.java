package com.uade.tpo.foodmarketplace.controllers.chefprofile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.uade.tpo.foodmarketplace.entity.chefprofile.ChefProfile;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.exceptions.chefprofile.ChefProfileNotFoundException;
import com.uade.tpo.foodmarketplace.service.chefprofile.ChefProfileService;

@ExtendWith(MockitoExtension.class)
class ChefProfilesControllerTest {
    @Mock ChefProfileService service;
    @InjectMocks ChefProfilesController controller;

    @Test void listaVaciaTieneContextoYDataNull() {
        when(service.getChefProfiles()).thenReturn(List.of());
        var response = controller.getChefProfiles();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().success());
        assertEquals("No hay perfiles de chefs disponibles", response.getBody().message());
        assertNull(response.getBody().data());
    }

    @Test void listaConDatosYReputacionConservanDtoYEnvelope() {
        User user = new User();
        user.setId(3L);
        ChefProfile profile = new ChefProfile();
        profile.setId(7L);
        profile.setUser(user);
        when(service.getChefProfiles()).thenReturn(List.of(profile));
        when(service.getReputacion(3L)).thenReturn(new BigDecimal("4.5"));
        var response = controller.getChefProfiles();
        assertEquals(7L, response.getBody().data().getFirst().id());
        assertEquals(new BigDecimal("4.5"), controller.reputacion(3L).getBody().data());
    }

    @Test void perfilInexistenteLanzaNotFoundDelDominio() {
        when(service.getChefProfileById(99L)).thenReturn(Optional.empty());
        assertThrows(ChefProfileNotFoundException.class, () -> controller.getChefProfile(99L));
    }
}
