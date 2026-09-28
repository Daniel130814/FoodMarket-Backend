package com.uade.tpo.foodmarketplace.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.uade.tpo.foodmarketplace.entity.user.User;

public interface UserRepository extends JpaRepository<User, Long> {

    /** Punto de sincronizaci\u00f3n estable para la creaci\u00f3n lazy del carrito. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    /**
     * Comprueba si un email ya está registrado, sin distinguir mayúsculas.
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Comprueba si otro usuario ya utiliza el email indicado.
     */
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
