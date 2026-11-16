package com.uade.tpo.foodmarketplace.service.favorito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.tpo.foodmarketplace.entity.favorito.Favorito;
import com.uade.tpo.foodmarketplace.entity.plato.EstadoPlato;
import com.uade.tpo.foodmarketplace.entity.plato.Plato;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.exceptions.favorito.FavoritoDuplicateException;
import com.uade.tpo.foodmarketplace.exceptions.favorito.FavoritoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.plato.PlatoNotFoundException;
import com.uade.tpo.foodmarketplace.repository.favorito.FavoritoRepository;
import com.uade.tpo.foodmarketplace.repository.plato.PlatoRepository;
import com.uade.tpo.foodmarketplace.security.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class FavoritoServiceImplTest {

    private static final long CLIENTE_ID = 1L;
    private static final long PLATO_ID = 7L;
    private static final List<EstadoPlato> PUBLICOS = List.of(EstadoPlato.PUBLICADO, EstadoPlato.AGOTADO);

    @Mock
    private FavoritoRepository favoritoRepository;
    @Mock
    private PlatoRepository platoRepository;
    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private FavoritoServiceImpl service;

    private User cliente;
    private Plato plato;

    @BeforeEach
    void setUp() {
        cliente = new User();
        cliente.setId(CLIENTE_ID);
        plato = new Plato();
        plato.setId(PLATO_ID);
        when(authenticatedUserService.getCurrentUser()).thenReturn(cliente);
    }

    @Test
    void agregaUnPlatoVisibleAFavoritos() {
        when(platoRepository.findByIdAndEstadoIn(PLATO_ID, PUBLICOS)).thenReturn(Optional.of(plato));
        when(favoritoRepository.existsByUsuarioIdAndPlatoId(CLIENTE_ID, PLATO_ID)).thenReturn(false);

        Plato resultado = service.agregar(PLATO_ID);

        assertSame(plato, resultado);
        verify(favoritoRepository).saveAndFlush(any(Favorito.class));
    }

    @Test
    void rechazaDuplicadoConConflicto() {
        when(platoRepository.findByIdAndEstadoIn(PLATO_ID, PUBLICOS)).thenReturn(Optional.of(plato));
        when(favoritoRepository.existsByUsuarioIdAndPlatoId(CLIENTE_ID, PLATO_ID)).thenReturn(true);

        FavoritoDuplicateException ex = assertThrows(FavoritoDuplicateException.class, () -> service.agregar(PLATO_ID));

        assertEquals("El plato ya está en tus favoritos", ex.getMessage());
        verify(favoritoRepository, never()).saveAndFlush(any());
    }

    @Test
    void noAgregaUnPlatoInexistenteONoVisible() {
        when(platoRepository.findByIdAndEstadoIn(PLATO_ID, PUBLICOS)).thenReturn(Optional.empty());

        assertThrows(PlatoNotFoundException.class, () -> service.agregar(PLATO_ID));
        verify(favoritoRepository, never()).saveAndFlush(any());
    }

    @Test
    void quitaUnFavoritoExistente() {
        Favorito favorito = new Favorito();
        when(favoritoRepository.findByUsuarioIdAndPlatoId(CLIENTE_ID, PLATO_ID)).thenReturn(Optional.of(favorito));

        service.quitar(PLATO_ID);

        verify(favoritoRepository).delete(favorito);
    }

    @Test
    void quitarUnFavoritoInexistenteDevuelveNotFound() {
        when(favoritoRepository.findByUsuarioIdAndPlatoId(CLIENTE_ID, PLATO_ID)).thenReturn(Optional.empty());

        assertThrows(FavoritoNotFoundException.class, () -> service.quitar(PLATO_ID));
    }

    @Test
    void listadoSinFavoritosDevuelveListaVacia() {
        when(favoritoRepository.findVisiblesByUsuarioId(CLIENTE_ID, PUBLICOS)).thenReturn(List.of());

        assertTrue(service.getFavoritos().isEmpty());
    }

    @Test
    void listadoDevuelveLosPlatosEnElOrdenDelRepositorio() {
        Plato otro = new Plato();
        otro.setId(9L);
        Favorito f1 = new Favorito();
        f1.setPlato(otro);
        Favorito f2 = new Favorito();
        f2.setPlato(plato);
        when(favoritoRepository.findVisiblesByUsuarioId(CLIENTE_ID, PUBLICOS)).thenReturn(List.of(f1, f2));

        assertEquals(List.of(otro, plato), service.getFavoritos());
    }
}
