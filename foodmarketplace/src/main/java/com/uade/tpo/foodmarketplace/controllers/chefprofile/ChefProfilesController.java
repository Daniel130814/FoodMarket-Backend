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
import com.uade.tpo.foodmarketplace.entity.dto.common.ApiResponse;
import com.uade.tpo.foodmarketplace.entity.dto.common.ResponseMapper;
import com.uade.tpo.foodmarketplace.exceptions.chefprofile.ChefProfileNotFoundException;
import com.uade.tpo.foodmarketplace.service.chefprofile.ChefProfileService;

@RestController
@RequestMapping("chef-profiles")
public class ChefProfilesController {

    private final ChefProfileService chefProfileService;

    public ChefProfilesController(ChefProfileService chefProfileService) {
        this.chefProfileService = chefProfileService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChefProfileResponse>>> getChefProfiles() {
        List<ChefProfileResponse> profiles = chefProfileService.getChefProfiles().stream()
                .map(profile -> ResponseMapper.chefProfile(profile,
                        chefProfileService.getReputacion(profile.getUser().getId())))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(profiles.isEmpty()
                ? "No hay perfiles de chefs disponibles"
                : "Perfiles de chefs obtenidos correctamente", profiles.isEmpty() ? null : profiles));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChefProfileResponse>> getChefProfile(@PathVariable Long id) {
        var profile = chefProfileService.getChefProfileById(id).orElseThrow(ChefProfileNotFoundException::new);
        return ResponseEntity.ok(ApiResponse.ok("Perfil de chef obtenido correctamente",
                ResponseMapper.chefProfile(profile, chefProfileService.getReputacion(profile.getUser().getId()))));
    }

    @GetMapping("/user/{userId}/reputacion")
    public ResponseEntity<ApiResponse<BigDecimal>> reputacion(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("Reputación obtenida correctamente",
                chefProfileService.getReputacion(userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChefProfileResponse>> create(@Valid @RequestBody ChefProfileRequest request) {
        var profile = chefProfileService.createChefProfile(request);
        return ResponseEntity.created(URI.create("/chef-profiles/" + profile.getId()))
                .body(ApiResponse.ok("Perfil de chef creado correctamente",
                        ResponseMapper.chefProfile(profile, chefProfileService.getReputacion(profile.getUser().getId()))));
    }

    /**
     * Actualiza los datos descriptivos de un perfil de chef sin cambiar su usuario.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ChefProfileResponse>> update(@PathVariable Long id,
            @Valid @RequestBody ChefProfileUpdateRequest request) {
        var profile = chefProfileService.updateChefProfile(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Perfil de chef actualizado correctamente",
                ResponseMapper.chefProfile(profile, chefProfileService.getReputacion(profile.getUser().getId()))));
    }
}
