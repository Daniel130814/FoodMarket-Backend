package com.uade.tpo.foodmarketplace.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import com.uade.tpo.foodmarketplace.entity.ChefProfile;
import com.uade.tpo.foodmarketplace.entity.dto.ChefProfileRequest;

public interface ChefProfileService {
    List<ChefProfile> getChefProfiles();
    Optional<ChefProfile> getChefProfileById(Long id);
    ChefProfile createChefProfile(ChefProfileRequest request);
    BigDecimal getReputacion(Long chefId);
}
