package com.uade.tpo.foodmarketplace.service.plato;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.tpo.foodmarketplace.entity.dto.plato.PlatoIngredienteRequest;
import com.uade.tpo.foodmarketplace.entity.dto.plato.PlatoRequest;
import com.uade.tpo.foodmarketplace.entity.ingrediente.Ingrediente;
import com.uade.tpo.foodmarketplace.entity.plato.Plato;
import com.uade.tpo.foodmarketplace.entity.plato.PlatoIngrediente;
import com.uade.tpo.foodmarketplace.entity.plato.UnidadMedida;
import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.exceptions.common.BusinessRuleException;
import com.uade.tpo.foodmarketplace.repository.category.CategoryRepository;
import com.uade.tpo.foodmarketplace.repository.ingrediente.IngredienteRepository;
import com.uade.tpo.foodmarketplace.repository.order.DetallePedidoRepository;
import com.uade.tpo.foodmarketplace.repository.plato.PlatoRepository;
import com.uade.tpo.foodmarketplace.repository.resena.ResenaRepository;
import com.uade.tpo.foodmarketplace.repository.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class PlatoServiceImplTest {

    private static final long PLATO_ID = 10L;
    private static final long CHEF_ID = 20L;
    private static final long POLLO_ID = 1L;
    private static final long ARROZ_ID = 2L;
    private static final long TOMATE_ID = 3L;

    @Mock
    private PlatoRepository platoRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private IngredienteRepository ingredienteRepository;
    @Mock
    private DetallePedidoRepository detallePedidoRepository;
    @Mock
    private ResenaRepository resenaRepository;

    @InjectMocks
    private PlatoServiceImpl platoService;

    @Test
    void updatePlato_actualizaRelacionesExistentesSinCrearDuplicados() {
        Plato plato = platoCon(ingrediente("Pollo", POLLO_ID), new BigDecimal("250"));
        PlatoIngrediente relacionExistente = plato.getIngredientes().getFirst();
        prepararActualizacion(plato);

        platoService.updatePlato(PLATO_ID, requestCon(ingredienteRequest(POLLO_ID, "280")));

        assertEquals(1, plato.getIngredientes().size());
        assertSame(relacionExistente, plato.getIngredientes().getFirst());
        assertEquals(new BigDecimal("280"), relacionExistente.getCantidad());
        verify(ingredienteRepository, never()).findById(POLLO_ID);
    }

    @Test
    void updatePlato_eliminaRelacionesAusentesYAgregaSoloLasNuevas() {
        Plato plato = platoCon(ingrediente("Pollo", POLLO_ID), new BigDecimal("250"));
        PlatoIngrediente pollo = plato.getIngredientes().getFirst();
        plato.getIngredientes().add(relacion(plato, ingrediente("Arroz", ARROZ_ID), new BigDecimal("150")));
        prepararActualizacion(plato);
        Ingrediente tomate = ingrediente("Tomate", TOMATE_ID);
        when(ingredienteRepository.findById(TOMATE_ID)).thenReturn(Optional.of(tomate));

        platoService.updatePlato(PLATO_ID, requestCon(
                ingredienteRequest(POLLO_ID, "280"),
                ingredienteRequest(TOMATE_ID, "100")));

        assertEquals(2, plato.getIngredientes().size());
        assertSame(pollo, plato.getIngredientes().stream()
                .filter(item -> item.getIngrediente().getId().equals(POLLO_ID))
                .findFirst().orElseThrow());
        assertFalse(plato.getIngredientes().stream()
                .anyMatch(item -> item.getIngrediente().getId().equals(ARROZ_ID)));
        PlatoIngrediente tomateAgregado = plato.getIngredientes().stream()
                .filter(item -> item.getIngrediente().getId().equals(TOMATE_ID))
                .findFirst().orElseThrow();
        assertSame(plato, tomateAgregado.getPlato());
        assertEquals(new BigDecimal("100"), tomateAgregado.getCantidad());
    }

    @Test
    void updatePlato_rechazaIngredientesDuplicadosSinModificarLaColeccion() {
        Plato plato = platoCon(ingrediente("Pollo", POLLO_ID), new BigDecimal("250"));
        PlatoIngrediente relacionExistente = plato.getIngredientes().getFirst();
        when(platoRepository.findById(PLATO_ID)).thenReturn(Optional.of(plato));

        assertThrows(BusinessRuleException.class, () -> platoService.updatePlato(PLATO_ID, requestCon(
                ingredienteRequest(POLLO_ID, "280"),
                ingredienteRequest(POLLO_ID, "300"))));

        assertEquals(1, plato.getIngredientes().size());
        assertSame(relacionExistente, plato.getIngredientes().getFirst());
        assertEquals(new BigDecimal("250"), relacionExistente.getCantidad());
        verify(platoRepository, never()).save(any());
        verifyNoInteractions(userRepository, categoryRepository, ingredienteRepository);
    }

    private void prepararActualizacion(Plato plato) {
        User chef = new User();
        chef.setRole(Role.CHEF);
        when(platoRepository.findById(PLATO_ID)).thenReturn(Optional.of(plato));
        when(userRepository.findById(CHEF_ID)).thenReturn(Optional.of(chef));
        when(platoRepository.save(plato)).thenReturn(plato);
    }

    private Plato platoCon(Ingrediente ingrediente, BigDecimal cantidad) {
        Plato plato = new Plato();
        plato.getIngredientes().add(relacion(plato, ingrediente, cantidad));
        return plato;
    }

    private PlatoIngrediente relacion(Plato plato, Ingrediente ingrediente, BigDecimal cantidad) {
        PlatoIngrediente relacion = new PlatoIngrediente();
        relacion.setPlato(plato);
        relacion.setIngrediente(ingrediente);
        relacion.setCantidad(cantidad);
        relacion.setUnidadMedida(UnidadMedida.GRAMOS);
        return relacion;
    }

    private Ingrediente ingrediente(String nombre, long id) {
        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setId(id);
        ingrediente.setNombre(nombre);
        return ingrediente;
    }

    private PlatoIngredienteRequest ingredienteRequest(long ingredienteId, String cantidad) {
        PlatoIngredienteRequest request = new PlatoIngredienteRequest();
        request.setIngredienteId(ingredienteId);
        request.setCantidad(new BigDecimal(cantidad));
        request.setUnidadMedida(UnidadMedida.GRAMOS);
        return request;
    }

    private PlatoRequest requestCon(PlatoIngredienteRequest... ingredientes) {
        PlatoRequest request = new PlatoRequest();
        request.setNombre("Plato de prueba");
        request.setPrecio(BigDecimal.TEN);
        request.setStockDisponible(1);
        request.setChefId(CHEF_ID);
        request.setCategoriasIds(List.<Long>of());
        request.setIngredientes(List.of(ingredientes));
        return request;
    }
}
