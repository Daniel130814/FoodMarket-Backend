package com.uade.tpo.foodmarketplace.controllers.plato;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;
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
import com.uade.tpo.foodmarketplace.entity.dto.common.ApiResponse;
import com.uade.tpo.foodmarketplace.entity.dto.common.ResponseMapper;
import com.uade.tpo.foodmarketplace.service.plato.PlatoService;
import com.uade.tpo.foodmarketplace.exceptions.plato.PlatoNotFoundException;

@RestController
@RequestMapping("platos")
public class PlatosController {

    @Autowired
    private PlatoService platoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlatoResponse>>> getPlatos(
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "categoriaId", required = false) Long categoriaId,
            @RequestParam(name = "precioMin", required = false) BigDecimal precioMin,
            @RequestParam(name = "precioMax", required = false) BigDecimal precioMax) {
        List<PlatoResponse> platos = platoService.getPlatos(nombre, categoriaId, precioMin, precioMax).stream()
                .map(ResponseMapper::plato).toList();
        boolean hayFiltros = (nombre != null && !nombre.isBlank()) || categoriaId != null || precioMin != null
                || precioMax != null;
        String message = platos.isEmpty()
                ? (hayFiltros ? "No se encontraron platos con los filtros indicados" : "No se encontraron platos disponibles")
                : "Platos obtenidos correctamente";
        return ResponseEntity.ok(ApiResponse.ok(message, platos.isEmpty() ? null : platos));
    }

    /** Platos del chef autenticado en todos los estados; la ruta literal gana sobre /{platoId} y tiene regla propia en SecurityConfig. */
    @GetMapping("/mis-platos")
    public ResponseEntity<ApiResponse<List<PlatoResponse>>> getMisPlatos() {
        List<PlatoResponse> platos = platoService.getMisPlatos().stream().map(ResponseMapper::plato).toList();
        return ResponseEntity.ok(ApiResponse.ok(
                platos.isEmpty() ? "No tenés platos cargados" : "Platos obtenidos correctamente",
                platos.isEmpty() ? null : platos));
    }

    @GetMapping("/{platoId}")
    public ResponseEntity<ApiResponse<PlatoResponse>> getPlatoById(@PathVariable("platoId") Long platoId) {
        return ResponseEntity.ok(ApiResponse.ok("Plato obtenido correctamente",platoService.getPlatoById(platoId).map(ResponseMapper::plato).orElseThrow(PlatoNotFoundException::new)));
    }

    @PostMapping("createPlato")
    public ResponseEntity<ApiResponse<PlatoResponse>> createPlato(@Valid @RequestBody PlatoRequest platoRequest) {
        var result = platoService.createPlato(platoRequest);

        return ResponseEntity
                .created(URI.create("/platos/" + result.getId()))
                .body(ApiResponse.ok("Plato creado correctamente", ResponseMapper.plato(result)));
    }

    /**
     * Actualiza los datos editables y las relaciones de receta de un plato existente.
     */
    @PutMapping("/{platoId}")
    public ResponseEntity<ApiResponse<PlatoResponse>> updatePlato(@PathVariable("platoId") Long platoId,
            @Valid @RequestBody PlatoRequest platoRequest) {
        return ResponseEntity.ok(ApiResponse.ok("Plato actualizado correctamente", ResponseMapper.plato(platoService.updatePlato(platoId, platoRequest))));
    }

    /**
     * Elimina un plato o lo pausa cuando los datos históricos aún lo referencian.
     */
    @DeleteMapping("/{platoId}")
    public ResponseEntity<ApiResponse<Void>> deletePlato(@PathVariable("platoId") Long platoId) {
        boolean eliminado = platoService.deletePlato(platoId);
        return ResponseEntity.ok(ApiResponse.ok(
                eliminado ? "Plato eliminado" : "Plato pausado porque tiene historial", null));
    }

    /** Sube una imagen (multipart, campo "file") y la agrega a imagenesUrls del plato. */
    @PostMapping(path = "/{platoId}/imagenes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PlatoResponse>> subirImagen(@PathVariable("platoId") Long platoId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity
                .created(URI.create("/platos/" + platoId))
                .body(ApiResponse.ok("Imagen subida correctamente",
                        ResponseMapper.plato(platoService.agregarImagen(platoId, file))));
    }

    /** Actualiza el descuento de un plato perteneciente al chef autenticado o a un administrador. */
    @PatchMapping("/{platoId}/descuento")
    public ResponseEntity<ApiResponse<PlatoResponse>> actualizarDescuento(@PathVariable("platoId") Long platoId,
            @Valid @RequestBody DescuentoPlatoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Descuento actualizado correctamente", ResponseMapper.plato(platoService.actualizarDescuento(platoId, request.porcentaje()))));
    }

    /** Quita el descuento de un plato sin eliminar la publicación. */
    @DeleteMapping("/{platoId}/descuento")
    public ResponseEntity<ApiResponse<PlatoResponse>> quitarDescuento(@PathVariable("platoId") Long platoId) {
        return ResponseEntity.ok(ApiResponse.ok("Descuento eliminado correctamente", ResponseMapper.plato(platoService.quitarDescuento(platoId))));
    }
}
