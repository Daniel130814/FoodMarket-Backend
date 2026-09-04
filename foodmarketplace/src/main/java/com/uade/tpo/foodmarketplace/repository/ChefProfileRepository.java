package com.uade.tpo.foodmarketplace.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.uade.tpo.foodmarketplace.entity.ChefProfile;

public interface ChefProfileRepository extends JpaRepository<ChefProfile, Long> {

    Optional<ChefProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
