package com.uade.tpo.foodmarketplace.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.tpo.foodmarketplace.entity.user.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
