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

import com.uade.tpo.foodmarketplace.entity.Resena;
import com.uade.tpo.foodmarketplace.entity.dto.ResenaRequest;
import com.uade.tpo.foodmarketplace.service.ResenaService;

@RestController
@RequestMapping("resenas")
public class ResenasController {

    @Autowired
    private ResenaService resenaService;

    @GetMapping
    public ResponseEntity<List<Resena>> getResenas() {
        return ResponseEntity.ok(resenaService.getResenas());
    }

    @GetMapping("/{resenaId}")
    public ResponseEntity<Resena> getResenaById(@PathVariable("resenaId") Long resenaId) {
        Optional<Resena> resena = resenaService.getResenaById(resenaId);

        if (resena.isPresent()) {
            return ResponseEntity.ok(resena.get());
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/plato/{platoId}")
    public ResponseEntity<List<Resena>> getResenasByPlatoId(@PathVariable("platoId") Long platoId) {
        return ResponseEntity.ok(resenaService.getResenasByPlatoId(platoId));
    }

    @PostMapping("createResena")
    public ResponseEntity<Resena> createResena(@RequestBody ResenaRequest resenaRequest) {
        Resena result = resenaService.createResena(
                resenaRequest.getCalificacion(),
                resenaRequest.getComentario(),
                resenaRequest.getClienteId(),
                resenaRequest.getPlatoId());

        return ResponseEntity
                .created(URI.create("/resenas/" + result.getId()))
                .body(result);
    }
}
