package com.uade.tpo.foodmarketplace.service;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.foodmarketplace.entity.User;

public interface UserService {

    List<User> getUsers();

    Optional<User> getUserById(Long userId);

    User createUser(String nombre, String apellido, String email);
}
