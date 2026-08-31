package com.uade.tpo.foodmarketplace.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.foodmarketplace.entity.User;
import com.uade.tpo.foodmarketplace.entity.Role;
import com.uade.tpo.foodmarketplace.exceptions.UserDuplicateException;
import com.uade.tpo.foodmarketplace.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public User createUser(String nombre, String apellido, String email, Role role)
            throws UserDuplicateException {

        List<User> users = userRepository.findAll();

        boolean userAlreadyExists = users.stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));

        if (userAlreadyExists) {
            throw new UserDuplicateException();
        }

        User user = new User();
        user.setNombre(nombre);
        user.setApellido(apellido);
        user.setEmail(email);
        user.setRole(role == null ? Role.CLIENTE : role);

        return userRepository.save(user);
    }
}
