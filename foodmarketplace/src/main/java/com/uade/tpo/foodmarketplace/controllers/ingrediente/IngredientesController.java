package com.uade.tpo.foodmarketplace.controllers.ingrediente;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.uade.tpo.foodmarketplace.entity.ingrediente.Ingrediente;
import com.uade.tpo.foodmarketplace.entity.dto.ingrediente.IngredienteRequest;
import com.uade.tpo.foodmarketplace.service.ingrediente.IngredienteService;

@RestController
@RequestMapping("ingredientes")
public class IngredientesController {

    @Autowired
    private IngredienteService ingredienteService;

    @GetMapping
    public ResponseEntity<List<Ingrediente>> getIngredientes() {
        return ResponseEntity.ok(ingredienteService.getIngredientes());
    }

    @GetMapping("/{ingredienteId}")
    public ResponseEntity<Ingrediente> getIngredienteById(
            @PathVariable("ingredienteId") Long ingredienteId) {
        Optional<Ingrediente> ingrediente = ingredienteService.getIngredienteById(ingredienteId);

        if (ingrediente.isPresent()) {
            return ResponseEntity.ok(ingrediente.get());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("createIngrediente")
    public ResponseEntity<Ingrediente> createIngrediente(@Valid @RequestBody IngredienteRequest ingredienteRequest) {
        Ingrediente result = ingredienteService.createIngrediente(
                ingredienteRequest.getNombre(),
                ingredienteRequest.getDescripcion());

        return ResponseEntity
                .created(URI.create("/ingredientes/" + result.getId()))
                .body(result);
    }

    /**
     * Actualiza el nombre y la descripción obligatorios de un ingrediente.
     */
    @PutMapping("/{ingredienteId}")
    public ResponseEntity<Ingrediente> updateIngrediente(@PathVariable("ingredienteId") Long ingredienteId,
            @Valid @RequestBody IngredienteRequest ingredienteRequest) {
        Ingrediente result = ingredienteService.updateIngrediente(ingredienteId, ingredienteRequest.getNombre(),
                ingredienteRequest.getDescripcion());
        return ResponseEntity.ok(result);
    }

    /**
     * Elimina un ingrediente cuando ninguna receta de plato lo referencia.
     */
    @DeleteMapping("/{ingredienteId}")
    public ResponseEntity<Void> deleteIngrediente(@PathVariable("ingredienteId") Long ingredienteId) {
        ingredienteService.deleteIngrediente(ingredienteId);
        return ResponseEntity.noContent().build();
    }
}
