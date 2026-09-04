package com.uade.tpo.foodmarketplace.service.plato;

import java.util.List;
import java.util.HashSet;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.foodmarketplace.entity.category.Category;
import com.uade.tpo.foodmarketplace.entity.plato.EstadoPlato;
import com.uade.tpo.foodmarketplace.entity.ingrediente.Ingrediente;
import com.uade.tpo.foodmarketplace.entity.plato.Plato;
import com.uade.tpo.foodmarketplace.entity.plato.PlatoIngrediente;
import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.entity.dto.plato.PlatoRequest;
import com.uade.tpo.foodmarketplace.exceptions.common.BusinessRuleException;
import com.uade.tpo.foodmarketplace.exceptions.category.CategoryNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.ingrediente.IngredienteNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.plato.PlatoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.user.UserNotFoundException;
import com.uade.tpo.foodmarketplace.repository.category.CategoryRepository;
import com.uade.tpo.foodmarketplace.repository.ingrediente.IngredienteRepository;
import com.uade.tpo.foodmarketplace.repository.order.DetallePedidoRepository;
import com.uade.tpo.foodmarketplace.repository.plato.PlatoRepository;
import com.uade.tpo.foodmarketplace.repository.resena.ResenaRepository;
import com.uade.tpo.foodmarketplace.repository.user.UserRepository;

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

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private ResenaRepository resenaRepository;

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
        Plato plato = new Plato();
        actualizarDatosPlato(plato, request, true);

        return platoRepository.save(plato);
    }

    /**
     * Updates a dish and replaces its category and ingredient associations atomically.
     */
    @Override
    @Transactional
    public Plato updatePlato(Long platoId, PlatoRequest request) {
        // The current entity is loaded before changing collections so orphan removal works correctly.
        Plato plato = platoRepository.findById(platoId).orElseThrow(PlatoNotFoundException::new);
        actualizarDatosPlato(plato, request, false);
        return platoRepository.save(plato);
    }

    /**
     * Removes an unused dish or pauses it to keep order and review history intact.
     */
    @Override
    @Transactional
    public void deletePlato(Long platoId) {
        Plato plato = platoRepository.findById(platoId).orElseThrow(PlatoNotFoundException::new);
        boolean tieneHistorial = detallePedidoRepository.existsByPlatoId(platoId)
                || resenaRepository.existsByPlatoId(platoId);
        if (tieneHistorial) {
            // Soft deletion retains foreign-key references used by completed orders and reviews.
            plato.setEstado(EstadoPlato.PAUSADO);
            platoRepository.save(plato);
            return;
        }

        platoRepository.delete(plato);
    }

    /**
     * Copies request data into a dish and rebuilds the associations that belong to it.
     */
    private void actualizarDatosPlato(Plato plato, PlatoRequest request, boolean esNuevo) {
        User chef = obtenerChef(request.getChefId());
        plato.setNombre(request.getNombre());
        plato.setDescripcion(request.getDescripcion());
        plato.setImagenUrl(request.getImagenUrl());
        plato.setPrecio(request.getPrecio());
        plato.setStockDisponible(request.getStockDisponible());
        if (request.getEstado() != null) {
            plato.setEstado(request.getEstado());
        } else if (esNuevo) {
            plato.setEstado(EstadoPlato.BORRADOR);
        }
        plato.setChef(chef);

        // Resolve every requested category so an invalid id cannot silently be persisted.
        List<Category> categorias = obtenerCategorias(request.getCategoriasIds());
        plato.getCategorias().clear();
        plato.getCategorias().addAll(categorias);

        // Clearing first lets JPA remove obsolete recipe rows through orphanRemoval.
        plato.getIngredientes().clear();
        agregarIngredientes(plato, request);
    }

    /**
     * Obtains a chef user and verifies that the selected user has the CHEF role.
     */
    private User obtenerChef(Long chefId) {
        User chef = userRepository.findById(chefId).orElseThrow(UserNotFoundException::new);
        if (chef.getRole() != Role.CHEF) {
            throw new BusinessRuleException("El usuario indicado no tiene rol CHEF");
        }
        return chef;
    }

    /**
     * Resolves the requested category identifiers to managed category entities.
     */
    private List<Category> obtenerCategorias(List<Long> categoriasIds) {
        if (categoriasIds == null) {
            return List.of();
        }
        return categoriasIds.stream()
                .distinct()
                .map(id -> categoryRepository.findById(id).orElseThrow(CategoryNotFoundException::new))
                .toList();
    }

    /**
     * Recreates the dish recipe while rejecting repeated ingredients in the request.
     */
    private void agregarIngredientes(Plato plato, PlatoRequest request) {
        if (request.getIngredientes() == null) {
            return;
        }

        HashSet<Long> ingredientesRecibidos = new HashSet<>();
        request.getIngredientes().forEach(item -> {
            if (!ingredientesRecibidos.add(item.getIngredienteId())) {
                throw new BusinessRuleException("Un ingrediente no puede repetirse en el mismo plato");
            }
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
}
