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

import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.entity.dto.user.UserRequest;
import com.uade.tpo.foodmarketplace.entity.dto.user.UserUpdateRequest;
import com.uade.tpo.foodmarketplace.exceptions.user.UserDuplicateException;
import com.uade.tpo.foodmarketplace.service.user.UserService;

@RestController
@RequestMapping("users")
public class UsersController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable("userId") Long userId) {
        Optional<User> user = userService.getUserById(userId);

        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("createUser")
    public ResponseEntity<User> createUser(@Valid @RequestBody UserRequest userRequest)
            throws UserDuplicateException {
        User result = userService.createUser(
                userRequest.getNombre(),
                userRequest.getApellido(),
                userRequest.getEmail(),
                userRequest.getRole());

        return ResponseEntity
                .created(URI.create("/users/" + result.getId()))
                .body(result);
    }

    /**
     * Updates user profile data without permitting a role change.
     */
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable("userId") Long userId,
            @Valid @RequestBody UserUpdateRequest userRequest) {
        return ResponseEntity.ok(userService.updateUser(userId, userRequest));
    }
}
