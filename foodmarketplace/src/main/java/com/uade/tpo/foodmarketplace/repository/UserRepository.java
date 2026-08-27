package com.uade.tpo.foodmarketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.tpo.foodmarketplace.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
