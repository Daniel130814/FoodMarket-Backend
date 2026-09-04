package com.uade.tpo.foodmarketplace.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.tpo.foodmarketplace.entity.user.User;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Comprueba si un email ya está registrado, sin distinguir mayúsculas.
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Comprueba si otro usuario ya utiliza el email indicado.
     */
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
