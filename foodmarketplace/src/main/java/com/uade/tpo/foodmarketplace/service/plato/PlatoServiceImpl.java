package com.uade.tpo.foodmarketplace.service.plato;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.foodmarketplace.entity.category.Category;
import com.uade.tpo.foodmarketplace.entity.plato.EstadoPlato;
import com.uade.tpo.foodmarketplace.entity.ingrediente.Ingrediente;
import com.uade.tpo.foodmarketplace.entity.plato.Plato;
import com.uade.tpo.foodmarketplace.entity.plato.PlatoIngrediente;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.entity.dto.plato.PlatoRequest;
import com.uade.tpo.foodmarketplace.exceptions.common.BusinessRuleException;
import org.springframework.security.access.AccessDeniedException;
import com.uade.tpo.foodmarketplace.exceptions.category.CategoryNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.ingrediente.IngredienteNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.plato.PlatoNotFoundException;
import com.uade.tpo.foodmarketplace.repository.category.CategoryRepository;
import com.uade.tpo.foodmarketplace.repository.ingrediente.IngredienteRepository;
import com.uade.tpo.foodmarketplace.repository.order.DetallePedidoRepository;
import com.uade.tpo.foodmarketplace.repository.plato.PlatoRepository;
import com.uade.tpo.foodmarketplace.repository.resena.ResenaRepository;
import com.uade.tpo.foodmarketplace.security.AuthenticatedUserService;

@Service
public class PlatoServiceImpl implements PlatoService {

    @Autowired
    private PlatoRepository platoRepository;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

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
        return platoRepository.findByEstado(EstadoPlato.PUBLICADO);
    }

    @Override
    public Optional<Plato> getPlatoById(Long platoId) {
        return platoRepository.findByIdAndEstado(platoId, EstadoPlato.PUBLICADO);
    }

    @Override
    public Plato createPlato(PlatoRequest request) {
        User chef = authenticatedUserService.getCurrentUser();
        if (chef.getRole() != Role.CHEF) {
            throw new AccessDeniedException("Solo un CHEF puede crear platos");
        }
        Plato plato = new Plato();
        plato.setChef(chef);
        actualizarDatosPlato(plato, request, true);

        return platoRepository.save(plato);
    }

    /**
     * Actualiza un plato y reemplaza atómicamente sus asociaciones de categorías e ingredientes.
     */
    @Override
    @Transactional
    public Plato updatePlato(Long platoId, PlatoRequest request) {
        // La entidad actual se carga antes de cambiar colecciones para que orphanRemoval funcione correctamente.
        Plato plato = platoRepository.findById(platoId).orElseThrow(PlatoNotFoundException::new);
        User currentUser = authenticatedUserService.getCurrentUser();
        authenticatedUserService.requireOwnerOrAdmin(currentUser, plato.getChef().getId());
        actualizarDatosPlato(plato, request, false);
        return platoRepository.save(plato);
    }

    /**
     * Elimina un plato sin uso o lo pausa para conservar el historial de órdenes y reseñas.
     */
    @Override
    @Transactional
    public void deletePlato(Long platoId) {
        Plato plato = platoRepository.findById(platoId).orElseThrow(PlatoNotFoundException::new);
        User currentUser = authenticatedUserService.getCurrentUser();
        authenticatedUserService.requireOwnerOrAdmin(currentUser, plato.getChef().getId());
        boolean tieneHistorial = detallePedidoRepository.existsByPlatoId(platoId)
                || resenaRepository.existsByPlatoId(platoId);
        if (tieneHistorial) {
            // El borrado lógico conserva las referencias de claves foráneas usadas por órdenes y reseñas completadas.
            plato.setEstado(EstadoPlato.PAUSADO);
            platoRepository.save(plato);
            return;
        }

        platoRepository.delete(plato);
    }

    /**
     * Copia los datos de la solicitud en un plato y sincroniza las asociaciones que le pertenecen.
     */
    private void actualizarDatosPlato(Plato plato, PlatoRequest request, boolean esNuevo) {
        validarIngredientesSinDuplicados(request);

        plato.setNombre(request.getNombre());
        plato.setDescripcion(request.getDescripcion());
        plato.setImagenUrl(request.getImagenUrl());
        plato.setPrecio(request.getPrecio());
        plato.setStockDisponible(request.getStockDisponible());

        if (request.getEstado() != null) {
            if (request.getEstado() == EstadoPlato.PUBLICADO
                    || request.getEstado() == EstadoPlato.AGOTADO) {

                plato.setEstado(
                        request.getStockDisponible() == 0
                                ? EstadoPlato.AGOTADO
                                : EstadoPlato.PUBLICADO
                );

            } else {
                plato.setEstado(request.getEstado());
            }
        } else if (esNuevo) {
            plato.setEstado(EstadoPlato.BORRADOR);
        } else if (plato.getEstado() == EstadoPlato.PUBLICADO
                && request.getStockDisponible() == 0) {
            plato.setEstado(EstadoPlato.AGOTADO);
        } else if (plato.getEstado() == EstadoPlato.AGOTADO
                && request.getStockDisponible() > 0) {
            plato.setEstado(EstadoPlato.PUBLICADO);
        }

        // Se resuelve cada categoría solicitada para que un id inválido no se persista silenciosamente.
        List<Category> categorias = obtenerCategorias(request.getCategoriasIds());
        plato.getCategorias().clear();
        plato.getCategorias().addAll(categorias);

        actualizarIngredientes(plato, request);
    }

    /**
     * Resuelve los identificadores de categorías solicitados en entidades de categoría gestionadas.
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

    private void actualizarIngredientes(Plato plato, PlatoRequest request) {
        if (request.getIngredientes() == null) {
            // Una lista ausente conserva el comportamiento previo: elimina toda la receta mediante orphanRemoval.
            plato.getIngredientes().clear();
            return;
        }

        Set<Long> ingredientesRecibidos = new HashSet<>();
        Map<Long, PlatoIngrediente> relacionesExistentes = new HashMap<>();
        for (PlatoIngrediente relacion : plato.getIngredientes()) {
            relacionesExistentes.put(relacion.getIngrediente().getId(), relacion);
        }

        request.getIngredientes().forEach(item -> ingredientesRecibidos.add(item.getIngredienteId()));

        // No borramos toda la colección: las relaciones que ya no llegan se eliminan una a una por orphanRemoval.
        plato.getIngredientes().removeIf(relacion -> !ingredientesRecibidos.contains(relacion.getIngrediente().getId()));

        request.getIngredientes().forEach(item -> {
            PlatoIngrediente relacionExistente = relacionesExistentes.get(item.getIngredienteId());
            if (relacionExistente != null) {
                // Si la relación ya existe, reutilizamos la misma Entity y evitamos un INSERT duplicado.
                relacionExistente.setCantidad(item.getCantidad());
                relacionExistente.setUnidadMedida(item.getUnidadMedida());
                return;
            }

            Ingrediente ingrediente = ingredienteRepository.findById(item.getIngredienteId())
                    .orElseThrow(IngredienteNotFoundException::new);
            PlatoIngrediente nuevaRelacion = new PlatoIngrediente();
            nuevaRelacion.setPlato(plato);
            nuevaRelacion.setIngrediente(ingrediente);
            nuevaRelacion.setCantidad(item.getCantidad());
            nuevaRelacion.setUnidadMedida(item.getUnidadMedida());
            plato.getIngredientes().add(nuevaRelacion);
        });
    }

    private void validarIngredientesSinDuplicados(PlatoRequest request) {
        if (request.getIngredientes() == null) {
            return;
        }

        Set<Long> ingredientesRecibidos = new HashSet<>();
        for (var item : request.getIngredientes()) {
            if (!ingredientesRecibidos.add(item.getIngredienteId())) {
                throw new BusinessRuleException("Un ingrediente no puede repetirse en el mismo plato");
            }
        }
    }
}
