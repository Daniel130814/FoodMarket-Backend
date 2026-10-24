package com.uade.tpo.foodmarketplace.controllers.imagen;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.foodmarketplace.service.imagen.ImagenStorageService;

/** Sirve públicamente los archivos subidos; el éxito devuelve el binario, no el envoltorio JSON. */
@RestController
@RequestMapping("imagenes")
public class ImagenesController {

    @Autowired
    private ImagenStorageService imagenStorageService;

    @GetMapping("/{nombre:.+}")
    public ResponseEntity<Resource> getImagen(@PathVariable("nombre") String nombre) {
        Resource recurso = imagenStorageService.cargar(nombre);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imagenStorageService.tipoContenido(nombre)))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic().immutable())
                .header("X-Content-Type-Options", "nosniff")
                .body(recurso);
    }
}
