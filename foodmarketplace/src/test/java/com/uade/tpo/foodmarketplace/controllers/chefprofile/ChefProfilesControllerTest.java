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

    @Test void respuestaIncluyeNombreApellidoYNacionalidad() {
        User user = new User();
        user.setId(3L);
        user.setNombre("Ana");
        user.setApellido("Gómez");
        ChefProfile profile = new ChefProfile();
        profile.setId(7L);
        profile.setUser(user);
        profile.setNacionalidad("Argentina");
        when(service.getChefProfileById(7L)).thenReturn(Optional.of(profile));
        when(service.getReputacion(3L)).thenReturn(BigDecimal.ZERO);

        var data = controller.getChefProfile(7L).getBody().data();

        assertEquals("Ana", data.nombre());
        assertEquals("Gómez", data.apellido());
        assertEquals("Argentina", data.nacionalidad());
    }

    @Test void requestsRechazanLargosExcesivos() {
        var request = new com.uade.tpo.foodmarketplace.entity.dto.chefprofile.ChefProfileRequest();
        request.setEspecialidad("x".repeat(101));
        request.setNacionalidad("x".repeat(101));
        request.setBiografia("x".repeat(2001));
        request.setDescripcion("x".repeat(2001));
        var update = new com.uade.tpo.foodmarketplace.entity.dto.chefprofile.ChefProfileUpdateRequest();
        update.setNacionalidad("x".repeat(101));
        try (var factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            assertEquals(4, factory.getValidator().validate(request).size());
            assertEquals(1, factory.getValidator().validate(update).size());
            request.setEspecialidad("x".repeat(100));
            request.setNacionalidad("x".repeat(100));
            request.setBiografia("x".repeat(2000));
            request.setDescripcion("x".repeat(2000));
            assertEquals(0, factory.getValidator().validate(request).size());
        }
    }

    @Test void perfilInexistenteLanzaNotFoundDelDominio() {
        when(service.getChefProfileById(99L)).thenReturn(Optional.empty());
        assertThrows(ChefProfileNotFoundException.class, () -> controller.getChefProfile(99L));
    }
}
