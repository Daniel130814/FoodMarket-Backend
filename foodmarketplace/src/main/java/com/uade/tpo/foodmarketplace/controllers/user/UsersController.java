package com.uade.tpo.foodmarketplace.controllers.user;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.uade.tpo.foodmarketplace.entity.dto.user.UserRequest;
import com.uade.tpo.foodmarketplace.entity.dto.user.UserResponse;
import com.uade.tpo.foodmarketplace.entity.dto.user.UserUpdateRequest;
import com.uade.tpo.foodmarketplace.entity.dto.common.ApiResponse;
import com.uade.tpo.foodmarketplace.exceptions.user.UserDuplicateException;
import com.uade.tpo.foodmarketplace.exceptions.user.UserNotFoundException;
import com.uade.tpo.foodmarketplace.service.user.UserService;

@RestController
@RequestMapping("users")
public class UsersController {

    @Autowired
    private UserService userService;

    /**
     * Devuelve usuarios como DTOs seguros para no exponer directamente la entidad JPA.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsers() {
        List<UserResponse> users = userService.getUsers();
        return ResponseEntity.ok(ApiResponse.ok(users.isEmpty() ? "No hay usuarios registrados"
                : "Usuarios obtenidos correctamente", users.isEmpty() ? null : users));
    }

    /**
     * Devuelve la información pública de un usuario cuando existe.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("Usuario obtenido correctamente",
                userService.getUserById(userId).orElseThrow(UserNotFoundException::new)));
    }

    /**
     * Crea un usuario y devuelve solamente los datos seguros definidos por UserResponse.
     */
    @PostMapping("createUser")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserRequest userRequest)
            throws UserDuplicateException {
        UserResponse result = userService.createUser(
                userRequest.getUsername(),
                userRequest.getNombre(),
                userRequest.getApellido(),
                userRequest.getEmail(),
                userRequest.getPassword(),
                userRequest.getRole());

        return ResponseEntity
                .created(URI.create("/users/" + result.id()))
                .body(ApiResponse.ok("Usuario creado correctamente", result));
    }

    /**
     * Actualiza los datos de perfil de un usuario sin permitir cambiar su rol.
     */
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable("userId") Long userId,
            @Valid @RequestBody UserUpdateRequest userRequest) {
        return ResponseEntity.ok(ApiResponse.ok("Usuario actualizado correctamente",
                userService.updateUser(userId, userRequest)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me() {
        return ResponseEntity.ok(ApiResponse.ok("Usuario autenticado obtenido correctamente",
                userService.getCurrentUser()));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(@Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Usuario actualizado correctamente",
                userService.updateCurrentUser(request)));
    }
}
