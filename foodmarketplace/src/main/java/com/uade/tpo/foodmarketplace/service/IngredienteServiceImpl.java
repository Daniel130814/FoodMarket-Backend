package com.uade.tpo.foodmarketplace.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.foodmarketplace.entity.Ingrediente;
import com.uade.tpo.foodmarketplace.repository.IngredienteRepository;

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
        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setNombre(nombre);
        ingrediente.setDescripcion(descripcion);

        return ingredienteRepository.save(ingrediente);
    }
}
