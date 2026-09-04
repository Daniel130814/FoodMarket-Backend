package com.uade.tpo.foodmarketplace.service;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.foodmarketplace.entity.Category;
import com.uade.tpo.foodmarketplace.entity.EstadoPlato;
import com.uade.tpo.foodmarketplace.entity.Ingrediente;
import com.uade.tpo.foodmarketplace.entity.Plato;
import com.uade.tpo.foodmarketplace.entity.PlatoIngrediente;
import com.uade.tpo.foodmarketplace.entity.Role;
import com.uade.tpo.foodmarketplace.entity.User;
import com.uade.tpo.foodmarketplace.entity.dto.PlatoRequest;
import com.uade.tpo.foodmarketplace.exceptions.BusinessRuleException;
import com.uade.tpo.foodmarketplace.exceptions.CategoryNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.IngredienteNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.UserNotFoundException;
import com.uade.tpo.foodmarketplace.repository.CategoryRepository;
import com.uade.tpo.foodmarketplace.repository.IngredienteRepository;
import com.uade.tpo.foodmarketplace.repository.PlatoRepository;
import com.uade.tpo.foodmarketplace.repository.UserRepository;

@Service
public class PlatoServiceImpl implements PlatoService {

    @Autowired
    private PlatoRepository platoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Override
    public List<Plato> getPlatos() {
        return platoRepository.findAll();
    }

    @Override
    public Optional<Plato> getPlatoById(Long platoId) {
        return platoRepository.findById(platoId);
    }

    @Override
    public Plato createPlato(PlatoRequest request) {
        User chef = userRepository.findById(request.getChefId()).orElseThrow(UserNotFoundException::new);
        if (chef.getRole() != Role.CHEF) {
            throw new BusinessRuleException("El usuario indicado no tiene rol CHEF");
        }
        Plato plato = new Plato();
        plato.setNombre(request.getNombre());
        plato.setDescripcion(request.getDescripcion());
        plato.setImagenUrl(request.getImagenUrl());
        plato.setPrecio(request.getPrecio());
        plato.setStockDisponible(request.getStockDisponible());
        plato.setEstado(request.getEstado() == null ? EstadoPlato.BORRADOR : request.getEstado());
        plato.setChef(chef);
        List<Category> categorias = request.getCategoriasIds() == null ? List.of() : request.getCategoriasIds().stream()
                .distinct().map(id -> categoryRepository.findById(id).orElseThrow(CategoryNotFoundException::new)).toList();
        plato.setCategorias(new ArrayList<>(categorias));
        if (request.getIngredientes() != null) {
            request.getIngredientes().forEach(item -> {
                Ingrediente ingrediente = ingredienteRepository.findById(item.getIngredienteId())
                        .orElseThrow(IngredienteNotFoundException::new);
                PlatoIngrediente relacion = new PlatoIngrediente();
                relacion.setPlato(plato);
                relacion.setIngrediente(ingrediente);
                relacion.setCantidad(item.getCantidad());
                relacion.setUnidadMedida(item.getUnidadMedida());
                plato.getIngredientes().add(relacion);
            });
        }

        return platoRepository.save(plato);
    }
}
