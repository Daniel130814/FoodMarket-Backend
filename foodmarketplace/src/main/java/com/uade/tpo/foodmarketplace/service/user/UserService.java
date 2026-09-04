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
     * Actualiza los datos personales de un usuario conservando el rol asignado.
     */
    User updateUser(Long userId, UserUpdateRequest request);
}
