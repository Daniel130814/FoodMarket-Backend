package com.uade.tpo.foodmarketplace.service.ingrediente;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.foodmarketplace.entity.ingrediente.Ingrediente;
import com.uade.tpo.foodmarketplace.repository.ingrediente.IngredienteRepository;

/**
 * Completa nombre_normalizado en ingredientes cargados antes de existir esa columna, para que
 * la búsqueda por nombre también los encuentre.
 */
@Component
public class IngredienteNombreBackfill implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(IngredienteNombreBackfill.class);

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (Ingrediente ingrediente : ingredienteRepository.findByNombreNormalizadoIsNull()) {
            String normalizado = Ingrediente.normalizar(ingrediente.getNombre());
            boolean tomado = ingredienteRepository.findByNombreNormalizado(normalizado)
                    .filter(otro -> !otro.getId().equals(ingrediente.getId())).isPresent();
            if (tomado) {
                log.warn("Ingrediente {} ('{}') duplica otro por nombre normalizado; revisar a mano",
                        ingrediente.getId(), ingrediente.getNombre());
                continue;
            }
            ingrediente.setNombreNormalizado(normalizado);
            ingredienteRepository.saveAndFlush(ingrediente);
        }
    }
}
