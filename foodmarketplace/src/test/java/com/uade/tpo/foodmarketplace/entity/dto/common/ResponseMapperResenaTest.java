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

    @Test
    void platoIncluyeChefNombreConNombreYApellido() {
        User chef = new User();
        chef.setId(3L);
        chef.setNombre("Ana");
        chef.setApellido("Gómez");
        Plato plato = new Plato();
        plato.setId(5L);
        plato.setNombre("Milanesa");
        plato.setChef(chef);
        plato.setPrecio(java.math.BigDecimal.TEN);
        plato.setStockDisponible(1);

        assertEquals("Ana Gómez", ResponseMapper.plato(plato).chefNombre());
    }

    @Test
    void chefSinNombreCaeAlUsername() {
        User chef = new User();
        chef.setUsername("chef01");

        assertEquals("chef01", ResponseMapper.nombreCompleto(chef));
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
