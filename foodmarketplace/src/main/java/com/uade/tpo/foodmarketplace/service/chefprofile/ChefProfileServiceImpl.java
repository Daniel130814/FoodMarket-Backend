package com.uade.tpo.foodmarketplace.service.chefprofile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.uade.tpo.foodmarketplace.entity.chefprofile.ChefProfile;
import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.entity.dto.chefprofile.ChefProfileRequest;
import com.uade.tpo.foodmarketplace.entity.dto.chefprofile.ChefProfileUpdateRequest;
import com.uade.tpo.foodmarketplace.exceptions.common.BusinessRuleException;
import com.uade.tpo.foodmarketplace.exceptions.chefprofile.ChefProfileNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.user.UserNotFoundException;
import com.uade.tpo.foodmarketplace.repository.chefprofile.ChefProfileRepository;
import com.uade.tpo.foodmarketplace.repository.resena.ResenaRepository;
import com.uade.tpo.foodmarketplace.repository.user.UserRepository;
import com.uade.tpo.foodmarketplace.security.AuthenticatedUserService;

@Service
public class ChefProfileServiceImpl implements ChefProfileService {

    private final ChefProfileRepository chefProfileRepository;
    private final UserRepository userRepository;
    private final ResenaRepository resenaRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public ChefProfileServiceImpl(ChefProfileRepository chefProfileRepository, UserRepository userRepository,
            ResenaRepository resenaRepository, AuthenticatedUserService authenticatedUserService) {
        this.chefProfileRepository = chefProfileRepository;
        this.userRepository = userRepository;
        this.resenaRepository = resenaRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    @Override
    public List<ChefProfile> getChefProfiles() {
        return chefProfileRepository.findAll();
    }

    @Override
    public Optional<ChefProfile> getChefProfileById(Long id) {
        return chefProfileRepository.findById(id);
    }

    @Override
    public ChefProfile createChefProfile(ChefProfileRequest request) {
        User user = authenticatedUserService.getCurrentUser();

        if (user.getRole() != Role.CHEF) {
            throw new BusinessRuleException("El perfil solo puede pertenecer a un usuario CHEF");
        }

        if (chefProfileRepository.existsByUserId(user.getId())) {
            throw new BusinessRuleException("El chef ya posee un perfil");
        }

        ChefProfile profile = new ChefProfile();
        profile.setUser(user);
        profile.setBiografia(request.getBiografia());
        profile.setEspecialidad(request.getEspecialidad());
        profile.setFotoUrl(request.getFotoUrl());
        profile.setDescripcion(request.getDescripcion());

        return chefProfileRepository.save(profile);
    }

    /**
     * Actualiza los campos descriptivos del perfil preservando el usuario chef asociado.
     */
    @Override
    public ChefProfile updateChefProfile(Long id, ChefProfileUpdateRequest request) {
        // Se verifican ambos registros para evitar actualizar un perfil con un chef asociado inválido.
        ChefProfile profile = chefProfileRepository.findById(id)
                .orElseThrow(ChefProfileNotFoundException::new);
        authenticatedUserService.requireOwnerOrAdmin(authenticatedUserService.getCurrentUser(),
                profile.getUser().getId());
        User user = userRepository.findById(profile.getUser().getId()).orElseThrow(UserNotFoundException::new);
        if (user.getRole() != Role.CHEF) {
            throw new BusinessRuleException("El perfil solo puede pertenecer a un usuario CHEF");
        }

        profile.setBiografia(request.getBiografia());
        profile.setEspecialidad(request.getEspecialidad());
        profile.setFotoUrl(request.getFotoUrl());
        profile.setDescripcion(request.getDescripcion());
        return chefProfileRepository.save(profile);
    }

    @Override
    public BigDecimal getReputacion(Long chefId) {
        userRepository.findById(chefId).orElseThrow(UserNotFoundException::new);

        Double promedio = resenaRepository.findPromedioCalificacionesByChefId(chefId);

        return promedio == null ? BigDecimal.ZERO : BigDecimal.valueOf(promedio).setScale(2, RoundingMode.HALF_UP);
    }
}
