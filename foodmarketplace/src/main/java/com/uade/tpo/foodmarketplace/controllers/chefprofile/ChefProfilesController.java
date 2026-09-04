package com.uade.tpo.foodmarketplace.controllers.chefprofile;

import java.net.URI;
import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.foodmarketplace.entity.dto.chefprofile.ChefProfileRequest;
import com.uade.tpo.foodmarketplace.entity.dto.chefprofile.ChefProfileResponse;
import com.uade.tpo.foodmarketplace.entity.dto.chefprofile.ChefProfileUpdateRequest;
import com.uade.tpo.foodmarketplace.entity.dto.common.ResponseMapper;
import com.uade.tpo.foodmarketplace.service.chefprofile.ChefProfileService;

@RestController
@RequestMapping("chef-profiles")
public class ChefProfilesController {

    private final ChefProfileService chefProfileService;

    public ChefProfilesController(ChefProfileService chefProfileService) {
        this.chefProfileService = chefProfileService;
    }

    @GetMapping
    public List<ChefProfileResponse> getChefProfiles() {
        return chefProfileService.getChefProfiles().stream()
                .map(profile -> ResponseMapper.chefProfile(profile,
                        chefProfileService.getReputacion(profile.getUser().getId())))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChefProfileResponse> getChefProfile(@PathVariable Long id) {
        return chefProfileService.getChefProfileById(id)
                .map(profile -> ResponseMapper.chefProfile(profile,
                        chefProfileService.getReputacion(profile.getUser().getId())))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}/reputacion")
    public BigDecimal reputacion(@PathVariable Long userId) {
        return chefProfileService.getReputacion(userId);
    }

    @PostMapping
    public ResponseEntity<ChefProfileResponse> create(@Valid @RequestBody ChefProfileRequest request) {
        var profile = chefProfileService.createChefProfile(request);
        return ResponseEntity.created(URI.create("/chef-profiles/" + profile.getId()))
                .body(ResponseMapper.chefProfile(profile, chefProfileService.getReputacion(profile.getUser().getId())));
    }

    /**
     * Actualiza los datos descriptivos de un perfil de chef sin cambiar su usuario.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ChefProfileResponse> update(@PathVariable Long id,
            @Valid @RequestBody ChefProfileUpdateRequest request) {
        var profile = chefProfileService.updateChefProfile(id, request);
        return ResponseEntity.ok(ResponseMapper.chefProfile(profile,
                chefProfileService.getReputacion(profile.getUser().getId())));
    }
}
