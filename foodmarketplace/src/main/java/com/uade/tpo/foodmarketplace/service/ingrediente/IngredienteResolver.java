package com.uade.tpo.foodmarketplace.service.ingrediente;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.foodmarketplace.entity.ingrediente.Ingrediente;
import com.uade.tpo.foodmarketplace.exceptions.common.BusinessRuleException;
import com.uade.tpo.foodmarketplace.repository.ingrediente.IngredienteRepository;

/**
 * Resuelve ingredientes por nombre para las recetas de los platos. Participa siempre de la
 * transacción del plato: si el plato falla, los ingredientes nuevos se descartan con él.
 */
@Service
public class IngredienteResolver {

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public Ingrediente obtenerOCrearPorNombre(String nombre) {
        String nombreLimpio = nombre == null ? "" : nombre.trim();
        if (nombreLimpio.isEmpty()) {
            throw new BusinessRuleException("El nombre del ingrediente no puede estar vacio");
        }
        if (nombreLimpio.length() > Ingrediente.NOMBRE_MAX_LENGTH) {
            throw new BusinessRuleException("El nombre del ingrediente no puede superar los "
                    + Ingrediente.NOMBRE_MAX_LENGTH + " caracteres");
        }
        String normalizado = Ingrediente.normalizar(nombreLimpio);
        return ingredienteRepository.findByNombreNormalizado(normalizado)
                .orElseGet(() -> crear(nombreLimpio, normalizado));
    }

    private Ingrediente crear(String nombre, String normalizado) {
        // El insert ignora el duplicado de una carrera; la relectura bloqueante devuelve la fila ganadora.
        ingredienteRepository.insertarSiNoExiste(nombre, nombre, normalizado);
        return ingredienteRepository.findByNombreNormalizadoForShare(normalizado)
                .orElseThrow(() -> new IllegalStateException("No se pudo resolver el ingrediente " + nombre));
    }
}
