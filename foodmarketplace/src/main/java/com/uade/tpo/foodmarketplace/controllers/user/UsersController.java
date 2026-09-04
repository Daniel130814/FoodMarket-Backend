package com.uade.tpo.foodmarketplace.controllers.user;

import java.net.URI;
import java.util.List;
import java.util.Optional;

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
import com.uade.tpo.foodmarketplace.exceptions.user.UserDuplicateException;
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
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    /**
     * Devuelve la información pública de un usuario cuando existe.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("userId") Long userId) {
        Optional<UserResponse> user = userService.getUserById(userId);

        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * Crea un usuario y devuelve solamente los datos seguros definidos por UserResponse.
     */
    @PostMapping("createUser")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest)
            throws UserDuplicateException {
        UserResponse result = userService.createUser(
                userRequest.getNombre(),
                userRequest.getApellido(),
                userRequest.getEmail(),
                userRequest.getRole());

        return ResponseEntity
                .created(URI.create("/users/" + result.id()))
                .body(result);
    }

    /**
     * Actualiza los datos de perfil de un usuario sin permitir cambiar su rol.
     */
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable("userId") Long userId,
            @Valid @RequestBody UserUpdateRequest userRequest) {
        return ResponseEntity.ok(userService.updateUser(userId, userRequest));
    }
}
