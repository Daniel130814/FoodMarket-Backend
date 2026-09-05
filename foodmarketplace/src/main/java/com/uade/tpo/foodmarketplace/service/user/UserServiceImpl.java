package com.uade.tpo.foodmarketplace.service.user;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.entity.dto.common.ResponseMapper;
import com.uade.tpo.foodmarketplace.entity.dto.user.UserResponse;
import com.uade.tpo.foodmarketplace.entity.dto.user.UserUpdateRequest;
import com.uade.tpo.foodmarketplace.exceptions.user.UserDuplicateException;
import com.uade.tpo.foodmarketplace.exceptions.user.UserNotFoundException;
import com.uade.tpo.foodmarketplace.repository.user.UserRepository;
import com.uade.tpo.foodmarketplace.security.AuthenticatedUserService;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    /**
     * Obtiene todos los usuarios y convierte cada entidad en un DTO seguro de respuesta.
     */
    @Override
    public List<UserResponse> getUsers() {
        // Convertimos las entidades antes de salir del servicio para no exponerlas al controlador.
        return userRepository.findAll().stream().map(ResponseMapper::user).toList();
    }

    /**
     * Busca un usuario y convierte el resultado opcional en una respuesta segura.
     */
    @Override
    public Optional<UserResponse> getUserById(Long userId) {
        return userRepository.findById(userId).map(ResponseMapper::user);
    }

    /**
     * Crea un usuario y devuelve su DTO público en lugar de la entidad persistida.
     */
    @Override
    public UserResponse createUser(String nombre, String apellido, String email, String password, Role role)
            throws UserDuplicateException {

        // Una consulta derivada evita cargar todos los usuarios solo para validar un email único.
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new UserDuplicateException();
        }

        User user = new User();
        user.setNombre(nombre);
        user.setApellido(apellido);
        user.setEmail(email.trim().toLowerCase(java.util.Locale.ROOT));
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        return ResponseMapper.user(userRepository.save(user));
    }

    /**
     * Actualiza nombre, apellido y email de un usuario sin permitir un cambio de rol.
     */
    @Override
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        // Se carga la entidad gestionada para conservar su rol aunque un cliente envíe datos adicionales.
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(request.getEmail(), userId)) {
            throw new UserDuplicateException();
        }

        user.setNombre(request.getNombre());
        user.setApellido(request.getApellido());
        user.setEmail(request.getEmail());
        return ResponseMapper.user(userRepository.save(user));
    }

    @Override
    public UserResponse getCurrentUser() {
        return ResponseMapper.user(authenticatedUserService.getCurrentUser());
    }

    @Override
    public UserResponse updateCurrentUser(UserUpdateRequest request) {
        return updateUser(authenticatedUserService.getCurrentUser().getId(), request);
    }
}
