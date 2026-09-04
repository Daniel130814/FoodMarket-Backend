package com.uade.tpo.foodmarketplace.service.user;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.entity.dto.user.UserUpdateRequest;

public interface UserService {

    List<User> getUsers();

    Optional<User> getUserById(Long userId);

    User createUser(String nombre, String apellido, String email, Role role);

    /**
     * Updates a user's personal data while retaining the assigned role.
     */
    User updateUser(Long userId, UserUpdateRequest request);
}
