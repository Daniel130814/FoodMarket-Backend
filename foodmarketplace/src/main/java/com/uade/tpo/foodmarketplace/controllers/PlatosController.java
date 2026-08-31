package com.uade.tpo.foodmarketplace.controllers;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.foodmarketplace.entity.Plato;
import com.uade.tpo.foodmarketplace.entity.dto.PlatoRequest;
import com.uade.tpo.foodmarketplace.service.PlatoService;

@RestController
@RequestMapping("platos")
public class PlatosController {

    @Autowired
    private PlatoService platoService;

    @GetMapping
    public ResponseEntity<List<Plato>> getPlatos() {
        return ResponseEntity.ok(platoService.getPlatos());
    }

    @GetMapping("/{platoId}")
    public ResponseEntity<Plato> getPlatoById(@PathVariable("platoId") Long platoId) {
        Optional<Plato> plato = platoService.getPlatoById(platoId);

        if (plato.isPresent()) {
            return ResponseEntity.ok(plato.get());
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/weeklyMenu/{menuSemanalId}")
    public ResponseEntity<List<Plato>> getPlatosByMenuSemanalId(
            @PathVariable("menuSemanalId") Long menuSemanalId) {
        return ResponseEntity.ok(platoService.getPlatosByMenuSemanalId(menuSemanalId));
    }

    @PostMapping("createPlato")
    public ResponseEntity<Plato> createPlato(@RequestBody PlatoRequest platoRequest) {
        Plato result = platoService.createPlato(
                platoRequest.getNombre(),
                platoRequest.getDescripcion(),
                platoRequest.getIngredientesIds(),
                platoRequest.getDiaSemana(),
                platoRequest.getImagenUrl(),
                platoRequest.getMenuSemanalId());

        return ResponseEntity
                .created(URI.create("/platos/" + result.getId()))
                .body(result);
    }
}
