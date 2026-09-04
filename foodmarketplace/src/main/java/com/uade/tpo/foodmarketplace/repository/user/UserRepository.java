package com.uade.tpo.foodmarketplace.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.tpo.foodmarketplace.entity.user.User;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Checks whether an email is already registered, ignoring case.
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Checks whether another user already uses the supplied email.
     */
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
