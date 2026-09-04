package com.uade.tpo.foodmarketplace.service.ingrediente;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.foodmarketplace.entity.ingrediente.Ingrediente;
import com.uade.tpo.foodmarketplace.exceptions.common.ResourceInUseException;
import com.uade.tpo.foodmarketplace.exceptions.ingrediente.IngredienteDuplicateException;
import com.uade.tpo.foodmarketplace.exceptions.ingrediente.IngredienteNotFoundException;
import com.uade.tpo.foodmarketplace.repository.ingrediente.IngredienteRepository;

@Service
public class IngredienteServiceImpl implements IngredienteService {

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Override
    public List<Ingrediente> getIngredientes() {
        return ingredienteRepository.findAll();
    }

    @Override
    public Optional<Ingrediente> getIngredienteById(Long ingredienteId) {
        return ingredienteRepository.findById(ingredienteId);
    }

    @Override
    public Ingrediente createIngrediente(String nombre, String descripcion) {
        if (ingredienteRepository.existsByNombreIgnoreCase(nombre)) {
            throw new IngredienteDuplicateException();
        }

        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setNombre(nombre);
        ingrediente.setDescripcion(descripcion);

        return ingredienteRepository.save(ingrediente);
    }

    /**
     * Actualiza un ingrediente después de comprobar que su nombre siga siendo único.
     */
    @Override
    public Ingrediente updateIngrediente(Long ingredienteId, String nombre, String descripcion) {
        // Cargar el ingrediente garantiza que la actualización conserve el identificador original.
        Ingrediente ingrediente = ingredienteRepository.findById(ingredienteId)
                .orElseThrow(IngredienteNotFoundException::new);
        if (ingredienteRepository.existsByNombreIgnoreCaseAndIdNot(nombre, ingredienteId)) {
            throw new IngredienteDuplicateException();
        }

        ingrediente.setNombre(nombre);
        ingrediente.setDescripcion(descripcion);
        return ingredienteRepository.save(ingrediente);
    }

    /**
     * Elimina un ingrediente únicamente cuando ninguna receta lo referencia.
     */
    @Override
    public void deleteIngrediente(Long ingredienteId) {
        Ingrediente ingrediente = ingredienteRepository.findById(ingredienteId)
                .orElseThrow(IngredienteNotFoundException::new);
        if (ingredienteRepository.existsUsedInPlatos(ingredienteId)) {
            throw new ResourceInUseException("No se puede eliminar un ingrediente usado por uno o mas platos");
        }

        ingredienteRepository.delete(ingrediente);
    }
}
