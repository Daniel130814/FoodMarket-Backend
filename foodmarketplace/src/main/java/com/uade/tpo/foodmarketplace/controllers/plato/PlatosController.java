package com.uade.tpo.foodmarketplace.controllers.plato;

import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;

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

import com.uade.tpo.foodmarketplace.entity.dto.plato.PlatoRequest;
import com.uade.tpo.foodmarketplace.entity.dto.plato.PlatoResponse;
import com.uade.tpo.foodmarketplace.entity.dto.common.ResponseMapper;
import com.uade.tpo.foodmarketplace.service.plato.PlatoService;

@RestController
@RequestMapping("platos")
public class PlatosController {

    @Autowired
    private PlatoService platoService;

    @GetMapping
    public ResponseEntity<List<PlatoResponse>> getPlatos() {
        return ResponseEntity.ok(platoService.getPlatos().stream().map(ResponseMapper::plato).toList());
    }

    @GetMapping("/{platoId}")
    public ResponseEntity<PlatoResponse> getPlatoById(@PathVariable("platoId") Long platoId) {
        return platoService.getPlatoById(platoId).map(ResponseMapper::plato).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("createPlato")
    public ResponseEntity<PlatoResponse> createPlato(@Valid @RequestBody PlatoRequest platoRequest) {
        var result = platoService.createPlato(platoRequest);

        return ResponseEntity
                .created(URI.create("/platos/" + result.getId()))
                .body(ResponseMapper.plato(result));
    }

    /**
     * Updates the editable data and recipe relationships of an existing dish.
     */
    @PutMapping("/{platoId}")
    public ResponseEntity<PlatoResponse> updatePlato(@PathVariable("platoId") Long platoId,
            @Valid @RequestBody PlatoRequest platoRequest) {
        return ResponseEntity.ok(ResponseMapper.plato(platoService.updatePlato(platoId, platoRequest)));
    }

    /**
     * Deletes a dish or pauses it when historical data still references it.
     */
    @DeleteMapping("/{platoId}")
    public ResponseEntity<Void> deletePlato(@PathVariable("platoId") Long platoId) {
        platoService.deletePlato(platoId);
        return ResponseEntity.noContent().build();
    }
}
