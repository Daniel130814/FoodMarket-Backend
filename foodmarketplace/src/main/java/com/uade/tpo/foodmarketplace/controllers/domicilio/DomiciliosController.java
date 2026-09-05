package com.uade.tpo.foodmarketplace.controllers.domicilio;

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

import com.uade.tpo.foodmarketplace.entity.domicilio.Domicilio;
import com.uade.tpo.foodmarketplace.entity.dto.domicilio.DomicilioRequest;
import com.uade.tpo.foodmarketplace.entity.dto.domicilio.DomicilioUpdateRequest;
import com.uade.tpo.foodmarketplace.exceptions.user.UserNotFoundException;
import com.uade.tpo.foodmarketplace.service.domicilio.DomicilioService;

@RestController
@RequestMapping("domicilios")
public class DomiciliosController {

    @Autowired
    private DomicilioService domicilioService;

    @GetMapping
    public ResponseEntity<List<Domicilio>> getDomicilios() {
        return ResponseEntity.ok(domicilioService.getDomicilios());
    }

    @GetMapping("/{domicilioId}")
    public ResponseEntity<Domicilio> getDomicilioById(@PathVariable("domicilioId") Long domicilioId) {
        Optional<Domicilio> domicilio = domicilioService.getDomicilioById(domicilioId);

        if (domicilio.isPresent()) {
            return ResponseEntity.ok(domicilio.get());
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Domicilio>> getDomiciliosByUsuarioId(@PathVariable("usuarioId") Long usuarioId) {
        return ResponseEntity.ok(domicilioService.getDomiciliosByUsuarioId(usuarioId));
    }

    @PostMapping("createDomicilio")
    public ResponseEntity<Domicilio> createDomicilio(@Valid @RequestBody DomicilioRequest domicilioRequest)
            throws UserNotFoundException {
        Domicilio result = domicilioService.createDomicilio(
                domicilioRequest.getCalle(),
                domicilioRequest.getNumero(),
                domicilioRequest.getPiso(),
                domicilioRequest.getDepartamento(),
                domicilioRequest.getCiudad(),
                domicilioRequest.getProvincia(),
                domicilioRequest.getCodigoPostal(),
                domicilioRequest.getIndicacionesEntrega());

        return ResponseEntity
                .created(URI.create("/domicilios/" + result.getId()))
                .body(result);
    }

    /**
     * Actualiza un domicilio conservando deliberadamente su usuario propietario actual.
     */
    @PutMapping("/{domicilioId}")
    public ResponseEntity<Domicilio> updateDomicilio(@PathVariable("domicilioId") Long domicilioId,
            @Valid @RequestBody DomicilioUpdateRequest domicilioRequest) {
        return ResponseEntity.ok(domicilioService.updateDomicilio(domicilioId, domicilioRequest));
    }

    /**
     * Elimina un domicilio que no fue registrado en una orden.
     */
    @DeleteMapping("/{domicilioId}")
    public ResponseEntity<Void> deleteDomicilio(@PathVariable("domicilioId") Long domicilioId) {
        domicilioService.deleteDomicilio(domicilioId);
        return ResponseEntity.noContent().build();
    }
}
