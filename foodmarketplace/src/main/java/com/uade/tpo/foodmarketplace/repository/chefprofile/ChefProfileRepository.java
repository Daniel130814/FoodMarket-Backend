package com.uade.tpo.foodmarketplace.repository.chefprofile;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.uade.tpo.foodmarketplace.entity.chefprofile.ChefProfile;

public interface ChefProfileRepository extends JpaRepository<ChefProfile, Long> {

    Optional<ChefProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
