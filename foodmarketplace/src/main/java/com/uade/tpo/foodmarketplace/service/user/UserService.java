package com.uade.tpo.foodmarketplace.service.user;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.entity.dto.user.UserResponse;
import com.uade.tpo.foodmarketplace.entity.dto.user.UserUpdateRequest;

public interface UserService {

    /**
     * Obtiene usuarios como DTOs seguros para no exponer entidades JPA desde la API.
     */
    List<UserResponse> getUsers();

    /**
     * Busca un usuario y devuelve únicamente sus datos públicos de respuesta.
     */
    Optional<UserResponse> getUserById(Long userId);

    /**
     * Crea un usuario y lo devuelve mediante un DTO seguro.
     */
    UserResponse createUser(String nombre, String apellido, String email, String password, Role role);

    /**
     * Actualiza los datos personales de un usuario conservando el rol asignado.
     */
    UserResponse updateUser(Long userId, UserUpdateRequest request);

    UserResponse getCurrentUser();

    UserResponse updateCurrentUser(UserUpdateRequest request);
}
