package com.uade.tpo.foodmarketplace.controllers.favorito;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.foodmarketplace.entity.dto.common.ApiResponse;
import com.uade.tpo.foodmarketplace.entity.dto.common.ResponseMapper;
import com.uade.tpo.foodmarketplace.entity.dto.plato.PlatoResponse;
import com.uade.tpo.foodmarketplace.service.favorito.FavoritoService;

/** Favoritos del cliente autenticado; la regla de acceso (solo CLIENTE) vive en SecurityConfig. */
@RestController
@RequestMapping("favoritos")
public class FavoritosController {

    @Autowired
    private FavoritoService favoritoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlatoResponse>>> getFavoritos() {
        List<PlatoResponse> platos = favoritoService.getFavoritos().stream().map(ResponseMapper::plato).toList();
        return ResponseEntity.ok(ApiResponse.ok(
                platos.isEmpty() ? "No tenés platos favoritos" : "Favoritos obtenidos correctamente",
                platos.isEmpty() ? null : platos));
    }

    @PostMapping("/{platoId}")
    public ResponseEntity<ApiResponse<PlatoResponse>> agregar(@PathVariable("platoId") Long platoId) {
        PlatoResponse plato = ResponseMapper.plato(favoritoService.agregar(platoId));
        return ResponseEntity.created(URI.create("/favoritos/" + platoId))
                .body(ApiResponse.ok("Plato agregado a favoritos", plato));
    }

    @DeleteMapping("/{platoId}")
    public ResponseEntity<ApiResponse<Void>> quitar(@PathVariable("platoId") Long platoId) {
        favoritoService.quitar(platoId);
        return ResponseEntity.ok(ApiResponse.ok("Plato quitado de favoritos", null));
    }
}
