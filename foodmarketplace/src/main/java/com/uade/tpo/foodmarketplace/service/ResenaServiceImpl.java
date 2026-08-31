package com.uade.tpo.foodmarketplace.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.foodmarketplace.entity.Resena;
import com.uade.tpo.foodmarketplace.entity.Plato;
import com.uade.tpo.foodmarketplace.entity.User;
import com.uade.tpo.foodmarketplace.exceptions.CalificacionInvalidaException;
import com.uade.tpo.foodmarketplace.exceptions.PlatoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.ResenaDuplicateException;
import com.uade.tpo.foodmarketplace.exceptions.UserNotFoundException;
import com.uade.tpo.foodmarketplace.repository.PlatoRepository;
import com.uade.tpo.foodmarketplace.repository.ResenaRepository;
import com.uade.tpo.foodmarketplace.repository.UserRepository;

@Service
public class ResenaServiceImpl implements ResenaService {

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlatoRepository platoRepository;

    @Override
    public List<Resena> getResenas() {
        return resenaRepository.findAll();
    }

    @Override
    public Optional<Resena> getResenaById(Long resenaId) {
        return resenaRepository.findById(resenaId);
    }

    @Override
    public List<Resena> getResenasByPlatoId(Long platoId) {
        if (!platoRepository.existsById(platoId)) {
            throw new PlatoNotFoundException();
        }

        return resenaRepository.findByPlatoId(platoId);
    }

    @Override
    public Resena createResena(Integer calificacion, String comentario, Long clienteId, Long platoId) {
        if (calificacion == null || calificacion < 1 || calificacion > 5) {
            throw new CalificacionInvalidaException();
        }

        User cliente = userRepository.findById(clienteId)
                .orElseThrow(UserNotFoundException::new);

        Plato plato = platoRepository.findById(platoId)
                .orElseThrow(PlatoNotFoundException::new);

        if (resenaRepository.existsByClienteIdAndPlatoId(clienteId, platoId)) {
            throw new ResenaDuplicateException();
        }

        Resena resena = new Resena();
        resena.setCalificacion(calificacion);
        resena.setComentario(comentario);
        resena.setFechaCreacion(LocalDateTime.now());
        resena.setCliente(cliente);
        resena.setPlato(plato);

        return resenaRepository.save(resena);
    }
}
