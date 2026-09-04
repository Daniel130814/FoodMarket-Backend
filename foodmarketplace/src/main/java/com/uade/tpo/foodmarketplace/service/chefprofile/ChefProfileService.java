package com.uade.tpo.foodmarketplace.service.chefprofile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import com.uade.tpo.foodmarketplace.entity.chefprofile.ChefProfile;
import com.uade.tpo.foodmarketplace.entity.dto.chefprofile.ChefProfileRequest;
import com.uade.tpo.foodmarketplace.entity.dto.chefprofile.ChefProfileUpdateRequest;

public interface ChefProfileService {
    List<ChefProfile> getChefProfiles();
    Optional<ChefProfile> getChefProfileById(Long id);
    ChefProfile createChefProfile(ChefProfileRequest request);
    /**
     * Actualiza los datos de presentación de un perfil de chef existente.
     */
    ChefProfile updateChefProfile(Long id, ChefProfileUpdateRequest request);
    BigDecimal getReputacion(Long chefId);
}
