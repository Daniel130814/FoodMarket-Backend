package com.uade.tpo.foodmarketplace.service.user;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.entity.dto.user.UserUpdateRequest;
import com.uade.tpo.foodmarketplace.exceptions.user.UserDuplicateException;
import com.uade.tpo.foodmarketplace.exceptions.user.UserNotFoundException;
import com.uade.tpo.foodmarketplace.repository.user.UserRepository;

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

        // A derived query avoids loading every user just to validate a unique email.
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new UserDuplicateException();
        }

        User user = new User();
        user.setNombre(nombre);
        user.setApellido(apellido);
        user.setEmail(email);
        user.setRole(role == null ? Role.CLIENTE : role);

        return userRepository.save(user);
    }

    /**
     * Updates a user's name, surname, and email without allowing a role change.
     */
    @Override
    public User updateUser(Long userId, UserUpdateRequest request) {
        // Load the managed entity so its role is preserved even if a client sends extra data.
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(request.getEmail(), userId)) {
            throw new UserDuplicateException();
        }

        user.setNombre(request.getNombre());
        user.setApellido(request.getApellido());
        user.setEmail(request.getEmail());
        return userRepository.save(user);
    }
}
