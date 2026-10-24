package com.uade.tpo.foodmarketplace.service.user;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.entity.plato.EstadoPlato;
import com.uade.tpo.foodmarketplace.entity.order.EstadoPedido;
import com.uade.tpo.foodmarketplace.entity.dto.common.ResponseMapper;
import com.uade.tpo.foodmarketplace.entity.dto.user.UserResponse;
import com.uade.tpo.foodmarketplace.entity.dto.user.UserUpdateRequest;
import com.uade.tpo.foodmarketplace.exceptions.common.BusinessRuleException;
import com.uade.tpo.foodmarketplace.exceptions.common.ResourceInUseException;
import com.uade.tpo.foodmarketplace.exceptions.user.UserDuplicateException;
import com.uade.tpo.foodmarketplace.exceptions.user.UserNotFoundException;
import com.uade.tpo.foodmarketplace.repository.user.UserRepository;
import com.uade.tpo.foodmarketplace.repository.plato.PlatoRepository;
import com.uade.tpo.foodmarketplace.repository.order.SubPedidoChefRepository;
import com.uade.tpo.foodmarketplace.security.AuthenticatedUserService;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @Autowired
    private PlatoRepository platoRepository;

    @Autowired
    private SubPedidoChefRepository subPedidoChefRepository;

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
    @Transactional
    public UserResponse createUser(String username, String nombre, String apellido, String email, String password, Role role)
            throws UserDuplicateException {

        String normalizedUsername = username.trim().toLowerCase(java.util.Locale.ROOT);
        if (normalizedUsername.length() < 3 || normalizedUsername.length() > 50) {
            throw new BusinessRuleException("El username debe tener entre 3 y 50 caracteres");
        }
        String normalizedEmail = email.trim().toLowerCase(java.util.Locale.ROOT);

        // Una consulta derivada evita cargar todos los usuarios solo para validar un email único.
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new UserDuplicateException();
        }
        if (userRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new UserDuplicateException();
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setNombre(nombre);
        user.setApellido(apellido);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        return ResponseMapper.user(saveWithUniqueCheck(user));
    }

    /**
     * Actualiza nombre, apellido y email de un usuario sin permitir un cambio de rol.
     */
    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        // Se carga la entidad gestionada para conservar su rol aunque un cliente envíe datos adicionales.
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        String email = request.getEmail().trim().toLowerCase(java.util.Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, userId)) {
            throw new UserDuplicateException();
        }

        user.setNombre(request.getNombre());
        user.setApellido(request.getApellido());
        user.setEmail(email);
        return ResponseMapper.user(saveWithUniqueCheck(user));
    }

    /** Cambia solo el rol y conserva la contraseña y todas las relaciones del usuario. */
    @Override
    @Transactional
    public UserResponse updateRole(Long userId, Role role) {
        User currentUser = authenticatedUserService.getCurrentUser();
        // También se valida en el servicio para proteger llamadas que no pasen por el controlador.
        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Solo un administrador puede cambiar roles");
        }

        User user = userRepository.findByIdForUpdate(userId).orElseThrow(UserNotFoundException::new);
        if (user.getRole() == role) {
            return ResponseMapper.user(user);
        }
        if (currentUser.getId().equals(userId)) {
            throw new BusinessRuleException("No podés quitarte a vos mismo el rol ADMIN");
        }

        if (user.getRole() == Role.CHEF
                && (platoRepository.existsByChefIdAndEstadoIn(userId,
                        List.of(EstadoPlato.PUBLICADO, EstadoPlato.AGOTADO))
                    || subPedidoChefRepository.existsByChefIdAndEstadoIn(userId,
                        List.of(EstadoPedido.PENDIENTE, EstadoPedido.CONFIRMADO,
                                EstadoPedido.EN_PREPARACION, EstadoPedido.ENVIADO)))) {
            throw new ResourceInUseException(
                    "No se puede quitar el rol CHEF mientras tenga platos publicados o agotados, o subpedidos sin terminar");
        }

        user.setRole(role);
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

    @Override
    @Transactional
    public void changePassword(String actual, String nueva) {
        User user = authenticatedUserService.getCurrentUser();
        if (!passwordEncoder.matches(actual, user.getPassword())) {
            throw new BusinessRuleException("La contraseña actual es incorrecta");
        }
        user.setPassword(passwordEncoder.encode(nueva));
        userRepository.save(user);
    }

    private User saveWithUniqueCheck(User user) {
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw UserUniqueConstraint.translate(exception);
        }
    }
}
