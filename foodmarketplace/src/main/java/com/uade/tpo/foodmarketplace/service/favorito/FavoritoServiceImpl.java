package com.uade.tpo.foodmarketplace.service.favorito;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
public class FavoritoServiceImpl implements FavoritoService {

    private static final List<EstadoPlato> ESTADOS_PUBLICOS = List.of(EstadoPlato.PUBLICADO, EstadoPlato.AGOTADO);

    @Autowired
    private FavoritoRepository favoritoRepository;

    @Autowired
    private PlatoRepository platoRepository;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @Override
    @Transactional(readOnly = true)
    public List<Plato> getFavoritos() {
        User cliente = authenticatedUserService.getCurrentUser();
        return favoritoRepository.findVisiblesByUsuarioId(cliente.getId(), ESTADOS_PUBLICOS).stream()
                .map(Favorito::getPlato).toList();
    }

    @Override
    @Transactional
    public Plato agregar(Long platoId) {
        User cliente = authenticatedUserService.getCurrentUser();
        Plato plato = platoRepository.findByIdAndEstadoIn(platoId, ESTADOS_PUBLICOS)
                .orElseThrow(PlatoNotFoundException::new);
        if (favoritoRepository.existsByUsuarioIdAndPlatoId(cliente.getId(), platoId)) {
            throw new FavoritoDuplicateException();
        }
        Favorito favorito = new Favorito();
        favorito.setUsuario(cliente);
        favorito.setPlato(plato);
        favorito.setFechaCreacion(LocalDateTime.now());
        try {
            favoritoRepository.saveAndFlush(favorito);
        } catch (DataIntegrityViolationException ex) {
            // Request concurrente que ganó la constraint única (usuario, plato).
            throw new FavoritoDuplicateException();
        }
        return plato;
    }

    @Override
    @Transactional
    public void quitar(Long platoId) {
        User cliente = authenticatedUserService.getCurrentUser();
        Favorito favorito = favoritoRepository.findByUsuarioIdAndPlatoId(cliente.getId(), platoId)
                .orElseThrow(FavoritoNotFoundException::new);
        favoritoRepository.delete(favorito);
    }
}
