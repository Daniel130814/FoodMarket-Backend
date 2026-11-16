package com.uade.tpo.foodmarketplace.entity.dto.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.uade.tpo.foodmarketplace.entity.dto.resena.ResenaResponse;
import com.uade.tpo.foodmarketplace.entity.plato.Plato;
import com.uade.tpo.foodmarketplace.entity.resena.Resena;
import com.uade.tpo.foodmarketplace.entity.user.User;

class ResponseMapperResenaTest {

    @Test
    void incluyeNombreConInicialDelApellidoYNombreDelPlato() {
        ResenaResponse response = ResponseMapper.resena(resena("Juan", "Pérez", "Milanesa"));

        assertEquals("Juan P.", response.clienteNombre());
        assertEquals("Milanesa", response.platoNombre());
        assertEquals(2L, response.clienteId());
        assertEquals(5L, response.platoId());
    }

    @Test
    void sinApellidoDevuelveSoloElNombre() {
        assertEquals("Juan", ResponseMapper.resena(resena("Juan", null, "Milanesa")).clienteNombre());
        assertEquals("Juan", ResponseMapper.resena(resena("Juan", "  ", "Milanesa")).clienteNombre());
    }

    private Resena resena(String nombre, String apellido, String platoNombre) {
        User cliente = new User();
        cliente.setId(2L);
        cliente.setNombre(nombre);
        cliente.setApellido(apellido);
        Plato plato = new Plato();
        plato.setId(5L);
        plato.setNombre(platoNombre);
        Resena resena = new Resena();
        resena.setId(1L);
        resena.setCliente(cliente);
        resena.setPlato(plato);
        resena.setCalificacion(5);
        return resena;
    }
}
