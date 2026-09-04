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

        // Una consulta derivada evita cargar todos los usuarios solo para validar un email único.
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
     * Actualiza nombre, apellido y email de un usuario sin permitir un cambio de rol.
     */
    @Override
    public User updateUser(Long userId, UserUpdateRequest request) {
        // Se carga la entidad gestionada para conservar su rol aunque un cliente envíe datos adicionales.
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
