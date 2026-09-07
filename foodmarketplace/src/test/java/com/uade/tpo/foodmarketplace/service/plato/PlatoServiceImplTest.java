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

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

import com.uade.tpo.foodmarketplace.entity.dto.common.ResponseMapper;
import com.uade.tpo.foodmarketplace.entity.dto.plato.PlatoIngredienteRequest;
import com.uade.tpo.foodmarketplace.entity.dto.plato.PlatoRequest;
import com.uade.tpo.foodmarketplace.entity.ingrediente.Ingrediente;
import com.uade.tpo.foodmarketplace.entity.plato.EstadoPlato;
import com.uade.tpo.foodmarketplace.entity.plato.Plato;
import com.uade.tpo.foodmarketplace.entity.plato.PlatoIngrediente;
import com.uade.tpo.foodmarketplace.entity.plato.UnidadMedida;
import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.exceptions.category.CategoryNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.common.BusinessRuleException;
import com.uade.tpo.foodmarketplace.repository.category.CategoryRepository;
import com.uade.tpo.foodmarketplace.repository.ingrediente.IngredienteRepository;
import com.uade.tpo.foodmarketplace.repository.order.DetallePedidoRepository;
import com.uade.tpo.foodmarketplace.repository.plato.PlatoRepository;
import com.uade.tpo.foodmarketplace.repository.resena.ResenaRepository;
import com.uade.tpo.foodmarketplace.security.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class PlatoServiceImplTest {

    private static final List<EstadoPlato> ESTADOS_PUBLICOS =
            List.of(EstadoPlato.PUBLICADO, EstadoPlato.AGOTADO);
    private static final long PLATO_ID = 10L;
    private static final long CHEF_ID = 20L;
    private static final long POLLO_ID = 1L;
    private static final long ARROZ_ID = 2L;
    private static final long TOMATE_ID = 3L;
    private static final String IMAGEN_1 = "https://ejemplo.com/plato-1.jpg";
    private static final String IMAGEN_2 = "https://ejemplo.com/plato-2.jpg";

    @Mock
    private PlatoRepository platoRepository;
    @Mock
    private AuthenticatedUserService authenticatedUserService;
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
    void getPlatos_sinFiltrosIncluyePublicadosYAgotadosPeroNoEstadosPrivados() {
        Plato publicado = new Plato();
        publicado.setEstado(EstadoPlato.PUBLICADO);
        Plato agotado = new Plato();
        agotado.setEstado(EstadoPlato.AGOTADO);
        List<Plato> platos = List.of(publicado, agotado);
        when(platoRepository.buscarConFiltros(ESTADOS_PUBLICOS, null, null, null, null))
                .thenReturn(platos);

        assertSame(platos, platoService.getPlatos(null, null, null, null));

        verify(platoRepository).buscarConFiltros(ESTADOS_PUBLICOS, null, null, null, null);
    }

    @Test
    void getPlatos_combinaFiltrosYNormalizaElNombre() {
        BigDecimal precioMin = new BigDecimal("5000");
        BigDecimal precioMax = new BigDecimal("12000");
        List<Plato> platos = List.of(new Plato());
        when(categoryRepository.existsById(2L)).thenReturn(true);
        when(platoRepository.buscarConFiltros(ESTADOS_PUBLICOS, "pollo", 2L, precioMin, precioMax))
                .thenReturn(platos);

        assertSame(platos, platoService.getPlatos("  pollo  ", 2L, precioMin, precioMax));

        verify(platoRepository).buscarConFiltros(ESTADOS_PUBLICOS, "pollo", 2L, precioMin, precioMax);
    }

    @Test
    void getPlatoById_permiteAgotado() {
        Plato agotado = new Plato();
        agotado.setEstado(EstadoPlato.AGOTADO);
        when(platoRepository.findByIdAndEstadoIn(PLATO_ID, ESTADOS_PUBLICOS)).thenReturn(Optional.of(agotado));

        assertSame(agotado, platoService.getPlatoById(PLATO_ID).orElseThrow());

        verify(platoRepository).findByIdAndEstadoIn(PLATO_ID, ESTADOS_PUBLICOS);
    }

    @Test
    void getPlatoById_excluyeBorradorYPausado() {
        when(platoRepository.findByIdAndEstadoIn(PLATO_ID, ESTADOS_PUBLICOS)).thenReturn(Optional.empty());

        assertFalse(platoService.getPlatoById(PLATO_ID).isPresent());

        verify(platoRepository).findByIdAndEstadoIn(PLATO_ID, ESTADOS_PUBLICOS);
    }

    @Test
    void getPlatos_rechazaPrecioMinimoNegativo() {
        assertThrows(BusinessRuleException.class,
                () -> platoService.getPlatos(null, null, new BigDecimal("-1"), null));

        verifyNoInteractions(platoRepository, categoryRepository);
    }

    @Test
    void getPlatos_rechazaPrecioMaximoNegativo() {
        assertThrows(BusinessRuleException.class,
                () -> platoService.getPlatos(null, null, null, new BigDecimal("-1")));

        verifyNoInteractions(platoRepository, categoryRepository);
    }

    @Test
    void getPlatos_rechazaRangoInvertido() {
        assertThrows(BusinessRuleException.class,
                () -> platoService.getPlatos(null, null, new BigDecimal("12000"), new BigDecimal("5000")));

        verifyNoInteractions(platoRepository, categoryRepository);
    }

    @Test
    void getPlatos_rechazaCategoriaInexistente() {
        when(categoryRepository.existsById(99L)).thenReturn(false);

        assertThrows(CategoryNotFoundException.class,
                () -> platoService.getPlatos(null, 99L, null, null));

        verifyNoInteractions(platoRepository);
    }

    @Test
    void createPlato_permiteBorradorSinImagenes() {
        prepararCreacionExitosa();

        Plato plato = platoService.createPlato(requestPara(EstadoPlato.BORRADOR, 1, null));

        assertEquals(EstadoPlato.BORRADOR, plato.getEstado());
        assertEquals(List.of(), plato.getImagenesUrls());
    }

    @Test
    void createPlato_rechazaPublicadoSinImagenes() {
        autenticarChef();

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> platoService.createPlato(requestPara(EstadoPlato.PUBLICADO, 1, List.of())));

        assertEquals("Un plato publicado debe tener al menos una imagen", exception.getMessage());
        verify(platoRepository, never()).save(any());
    }

    @Test
    void createPlato_permitePublicadoConUnaImagen() {
        prepararCreacionExitosa();

        Plato plato = platoService.createPlato(requestPara(EstadoPlato.PUBLICADO, 1, List.of(IMAGEN_1)));

        assertEquals(EstadoPlato.PUBLICADO, plato.getEstado());
        assertEquals(List.of(IMAGEN_1), plato.getImagenesUrls());
    }

    @Test
    void createPlato_permitePublicadoConVariasImagenesYConservaElOrden() {
        prepararCreacionExitosa();
        List<String> imagenes = List.of(IMAGEN_1, IMAGEN_2);

        Plato plato = platoService.createPlato(requestPara(EstadoPlato.PUBLICADO, 1, imagenes));

        assertEquals(imagenes, plato.getImagenesUrls());
        assertEquals(imagenes, ResponseMapper.plato(plato).imagenesUrls());
    }

    @Test
    void createPlato_rechazaAgotadoSinImagenes() {
        autenticarChef();

        assertThrows(BusinessRuleException.class,
                () -> platoService.createPlato(requestPara(EstadoPlato.PUBLICADO, 0, List.of())));

        verify(platoRepository, never()).save(any());
    }

    @Test
    void createPlato_permiteAgotadoConImagenes() {
        prepararCreacionExitosa();

        Plato plato = platoService.createPlato(requestPara(EstadoPlato.PUBLICADO, 0, List.of(IMAGEN_1)));

        assertEquals(EstadoPlato.AGOTADO, plato.getEstado());
        assertEquals(List.of(IMAGEN_1), plato.getImagenesUrls());
    }

    @Test
    void createPlato_permitePausadoSinImagenes() {
        prepararCreacionExitosa();

        Plato plato = platoService.createPlato(requestPara(EstadoPlato.PAUSADO, 1, List.of()));

        assertEquals(EstadoPlato.PAUSADO, plato.getEstado());
        assertEquals(List.of(), plato.getImagenesUrls());
    }

    @Test
    void updatePlato_rechazaDejarSinImagenesUnPlatoPublicado() {
        User chef = chef();
        Plato plato = new Plato();
        plato.setChef(chef);
        plato.setEstado(EstadoPlato.PUBLICADO);
        plato.setStockDisponible(1);
        plato.getImagenesUrls().add(IMAGEN_1);
        when(platoRepository.findById(PLATO_ID)).thenReturn(Optional.of(plato));
        when(authenticatedUserService.getCurrentUser()).thenReturn(chef);

        assertThrows(BusinessRuleException.class,
                () -> platoService.updatePlato(PLATO_ID,
                        requestPara(EstadoPlato.PUBLICADO, 1, List.of())));

        verify(platoRepository, never()).save(any());
    }

    @Test
    void platoRequest_rechazaImagenEnBlanco() {
        PlatoRequest request = requestPara(EstadoPlato.BORRADOR, 1, List.of("   "));

        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            assertEquals(1, validatorFactory.getValidator().validate(request).size());
        }
    }

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
        verifyNoInteractions(categoryRepository, ingredienteRepository);
    }

    private void prepararCreacionExitosa() {
        autenticarChef();
        when(platoRepository.save(any(Plato.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private User autenticarChef() {
        User chef = chef();
        when(authenticatedUserService.getCurrentUser()).thenReturn(chef);
        return chef;
    }

    private User chef() {
        User chef = new User();
        chef.setId(CHEF_ID);
        chef.setRole(Role.CHEF);
        return chef;
    }

    private PlatoRequest requestPara(EstadoPlato estado, int stock, List<String> imagenesUrls) {
        PlatoRequest request = requestCon();
        request.setEstado(estado);
        request.setStockDisponible(stock);
        request.setImagenesUrls(imagenesUrls);
        return request;
    }

    private void prepararActualizacion(Plato plato) {
        User chef = new User();
        chef.setRole(Role.CHEF);
        chef.setId(CHEF_ID);
        when(platoRepository.findById(PLATO_ID)).thenReturn(Optional.of(plato));
        when(authenticatedUserService.getCurrentUser()).thenReturn(chef);
        when(platoRepository.save(plato)).thenReturn(plato);
    }

    private Plato platoCon(Ingrediente ingrediente, BigDecimal cantidad) {
        Plato plato = new Plato();
        User chef = new User();
        chef.setId(CHEF_ID);
        chef.setRole(Role.CHEF);
        plato.setChef(chef);
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
        request.setCategoriasIds(List.<Long>of());
        request.setIngredientes(List.of(ingredientes));
        return request;
    }
}
