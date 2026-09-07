package com.uade.tpo.foodmarketplace.service.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.foodmarketplace.entity.dto.auth.AuthenticationRequest;
import com.uade.tpo.foodmarketplace.entity.dto.auth.AuthenticationResponse;
import com.uade.tpo.foodmarketplace.entity.dto.auth.RegisterRequest;
import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.exceptions.common.BusinessRuleException;
import com.uade.tpo.foodmarketplace.exceptions.user.UserDuplicateException;
import com.uade.tpo.foodmarketplace.exceptions.user.UserNotFoundException;
import com.uade.tpo.foodmarketplace.repository.user.UserRepository;
import com.uade.tpo.foodmarketplace.security.JwtService;

/** Gestiona registro y login; las contraseñas siempre se delegan a BCrypt. */
@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        String username = normalizeUsername(request.username());
        if (username.length() < 3 || username.length() > 50) {
            throw new BusinessRuleException("El username debe tener entre 3 y 50 caracteres");
        }
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new UserDuplicateException();
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new UserDuplicateException();
        }

        User user = new User();
        user.setNombre(request.nombre());
        user.setApellido(request.apellido());
        user.setUsername(username);
        user.setEmail(email);
        // Nunca persistimos la contraseña recibida: BCrypt almacena un hash con salt.
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.valueOf(request.tipoCuenta().name()));
        User saved = userRepository.save(user);
        return response(saved);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        String username = normalizeUsername(request.username());
        // AuthenticationManager usa el provider y BCrypt; no se comparan passwords manualmente.
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, request.password()));
        User user = userRepository.findByUsernameIgnoreCase(username).orElseThrow(UserNotFoundException::new);
        return response(user);
    }

    private AuthenticationResponse response(User user) {
        return new AuthenticationResponse(jwtService.generateToken(user), user.getId(), user.getRole());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
