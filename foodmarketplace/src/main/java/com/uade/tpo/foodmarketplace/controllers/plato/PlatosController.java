package com.uade.tpo.foodmarketplace.controllers.plato;

import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.foodmarketplace.entity.dto.plato.PlatoRequest;
import com.uade.tpo.foodmarketplace.entity.dto.plato.PlatoResponse;
import com.uade.tpo.foodmarketplace.entity.dto.plato.DescuentoPlatoRequest;
import com.uade.tpo.foodmarketplace.entity.dto.common.ResponseMapper;
import com.uade.tpo.foodmarketplace.service.plato.PlatoService;

@RestController
@RequestMapping("platos")
public class PlatosController {

    @Autowired
    private PlatoService platoService;

    @GetMapping
    public ResponseEntity<List<PlatoResponse>> getPlatos(
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "categoriaId", required = false) Long categoriaId,
            @RequestParam(name = "precioMin", required = false) BigDecimal precioMin,
            @RequestParam(name = "precioMax", required = false) BigDecimal precioMax) {
        return ResponseEntity.ok(platoService.getPlatos(nombre, categoriaId, precioMin, precioMax).stream()
                .map(ResponseMapper::plato).toList());
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
     * Actualiza los datos editables y las relaciones de receta de un plato existente.
     */
    @PutMapping("/{platoId}")
    public ResponseEntity<PlatoResponse> updatePlato(@PathVariable("platoId") Long platoId,
            @Valid @RequestBody PlatoRequest platoRequest) {
        return ResponseEntity.ok(ResponseMapper.plato(platoService.updatePlato(platoId, platoRequest)));
    }

    /**
     * Elimina un plato o lo pausa cuando los datos históricos aún lo referencian.
     */
    @DeleteMapping("/{platoId}")
    public ResponseEntity<Void> deletePlato(@PathVariable("platoId") Long platoId) {
        platoService.deletePlato(platoId);
        return ResponseEntity.noContent().build();
    }

    /** Actualiza el descuento de un plato perteneciente al chef autenticado o a un administrador. */
    @PatchMapping("/{platoId}/descuento")
    public ResponseEntity<PlatoResponse> actualizarDescuento(@PathVariable("platoId") Long platoId,
            @Valid @RequestBody DescuentoPlatoRequest request) {
        return ResponseEntity.ok(ResponseMapper.plato(platoService.actualizarDescuento(platoId, request.porcentaje())));
    }

    /** Quita el descuento de un plato sin eliminar la publicación. */
    @DeleteMapping("/{platoId}/descuento")
    public ResponseEntity<PlatoResponse> quitarDescuento(@PathVariable("platoId") Long platoId) {
        return ResponseEntity.ok(ResponseMapper.plato(platoService.quitarDescuento(platoId)));
    }
}
