package com.uade.tpo.foodmarketplace.controllers.resena;

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

import com.uade.tpo.foodmarketplace.entity.dto.resena.ResenaRequest;
import com.uade.tpo.foodmarketplace.entity.dto.resena.ResenaResponse;
import com.uade.tpo.foodmarketplace.entity.dto.resena.ResenaUpdateRequest;
import com.uade.tpo.foodmarketplace.entity.dto.common.ResponseMapper;
import com.uade.tpo.foodmarketplace.service.resena.ResenaService;

@RestController
@RequestMapping("resenas")
public class ResenasController {

    @Autowired
    private ResenaService resenaService;

    @GetMapping
    public ResponseEntity<List<ResenaResponse>> getResenas() {
        return ResponseEntity.ok(resenaService.getResenas().stream().map(ResponseMapper::resena).toList());
    }

    @GetMapping("/{resenaId}")
    public ResponseEntity<ResenaResponse> getResenaById(@PathVariable("resenaId") Long resenaId) {
        return resenaService.getResenaById(resenaId).map(ResponseMapper::resena).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/plato/{platoId}")
    public ResponseEntity<List<ResenaResponse>> getResenasByPlatoId(@PathVariable("platoId") Long platoId) {
        return ResponseEntity.ok(resenaService.getResenasByPlatoId(platoId).stream().map(ResponseMapper::resena).toList());
    }

    @PostMapping("createResena")
    public ResponseEntity<ResenaResponse> createResena(@Valid @RequestBody ResenaRequest resenaRequest) {
        var result = resenaService.createResena(
                resenaRequest.getCalificacion(),
                resenaRequest.getComentario(),
                resenaRequest.getPlatoId());

        return ResponseEntity
                .created(URI.create("/resenas/" + result.getId()))
                .body(ResponseMapper.resena(result));
    }

    /**
     * Actualiza únicamente la calificación y el comentario de una reseña existente.
     */
    @PutMapping("/{resenaId}")
    public ResponseEntity<ResenaResponse> updateResena(@PathVariable("resenaId") Long resenaId,
            @Valid @RequestBody ResenaUpdateRequest resenaRequest) {
        return ResponseEntity.ok(ResponseMapper.resena(resenaService.updateResena(resenaId, resenaRequest)));
    }

    /**
     * Elimina una reseña existente.
     */
    @DeleteMapping("/{resenaId}")
    public ResponseEntity<Void> deleteResena(@PathVariable("resenaId") Long resenaId) {
        resenaService.deleteResena(resenaId);
        return ResponseEntity.noContent().build();
    }
}
