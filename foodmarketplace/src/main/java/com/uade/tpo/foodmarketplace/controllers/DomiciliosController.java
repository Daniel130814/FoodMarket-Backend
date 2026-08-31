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

import com.uade.tpo.foodmarketplace.entity.Domicilio;
import com.uade.tpo.foodmarketplace.entity.dto.DomicilioRequest;
import com.uade.tpo.foodmarketplace.exceptions.UserNotFoundException;
import com.uade.tpo.foodmarketplace.service.DomicilioService;

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
    public ResponseEntity<Domicilio> createDomicilio(@RequestBody DomicilioRequest domicilioRequest)
            throws UserNotFoundException {
        Domicilio result = domicilioService.createDomicilio(
                domicilioRequest.getCalle(),
                domicilioRequest.getNumero(),
                domicilioRequest.getPiso(),
                domicilioRequest.getDepartamento(),
                domicilioRequest.getCiudad(),
                domicilioRequest.getProvincia(),
                domicilioRequest.getCodigoPostal(),
                domicilioRequest.getIndicacionesEntrega(),
                domicilioRequest.getPredeterminado(),
                domicilioRequest.getUsuarioId());

        return ResponseEntity
                .created(URI.create("/domicilios/" + result.getId()))
                .body(result);
    }
}
