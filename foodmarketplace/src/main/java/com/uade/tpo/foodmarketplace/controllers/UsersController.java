package com.uade.tpo.foodmarketplace.controllers;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.foodmarketplace.entity.User;
import com.uade.tpo.foodmarketplace.entity.dto.UserRequest;
import com.uade.tpo.foodmarketplace.exceptions.UserDuplicateException;
import com.uade.tpo.foodmarketplace.service.UserService;

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
    public ResponseEntity<User> createUser(@RequestBody UserRequest userRequest)
            throws UserDuplicateException {
        User result = userService.createUser(
                userRequest.getNombre(),
                userRequest.getApellido(),
                userRequest.getEmail());

        return ResponseEntity
                .created(URI.create("/users/" + result.getId()))
                .body(result);
    }
}
