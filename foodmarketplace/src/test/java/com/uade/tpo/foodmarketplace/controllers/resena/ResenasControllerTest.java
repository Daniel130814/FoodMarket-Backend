package com.uade.tpo.foodmarketplace.controllers.resena;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.tpo.foodmarketplace.entity.dto.common.ApiResponse;
import com.uade.tpo.foodmarketplace.entity.dto.resena.ResenaResponse;
import com.uade.tpo.foodmarketplace.entity.plato.Plato;
import com.uade.tpo.foodmarketplace.entity.resena.Resena;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.service.resena.ResenaService;

@ExtendWith(MockitoExtension.class)
class ResenasControllerTest {

    @Mock
    private ResenaService resenaService;

    @InjectMocks
    private ResenasController resenasController;

    @Test
    void listaConElementosIncluyeContextoYDatos() {
        when(resenaService.getResenas()).thenReturn(List.of(resena(1L)));

        ApiResponse<List<ResenaResponse>> response = resenasController.getResenas().getBody();

        assertEquals(true, response.success());
        assertEquals("Reseñas obtenidas correctamente", response.message());
        assertEquals(1, response.data().size());
    }

    @Test
    void listaVaciaPorPlatoSigueSiendoExitosaYExplicaElResultado() {
        when(resenaService.getResenasByPlatoId(5L)).thenReturn(List.of());

        ApiResponse<List<ResenaResponse>> response = resenasController.getResenasByPlatoId(5L).getBody();

        assertEquals(true, response.success());
        assertEquals("Todavía no hay reseñas para este plato", response.message());
        assertEquals(List.of(), response.data());
    }

    private Resena resena(Long id) {
        User cliente = new User();
        cliente.setId(2L);
        Plato plato = new Plato();
        plato.setId(5L);
        Resena resena = new Resena();
        resena.setId(id);
        resena.setCliente(cliente);
        resena.setPlato(plato);
        resena.setCalificacion(5);
        return resena;
    }
}
