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
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new UserDuplicateException();
        }

        User user = new User();
        user.setNombre(request.nombre());
        user.setApellido(request.apellido());
        user.setEmail(email);
        // Nunca persistimos la contraseña recibida: BCrypt almacena un hash con salt.
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.valueOf(request.tipoCuenta().name()));
        User saved = userRepository.save(user);
        return response(saved);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        String email = normalizeEmail(request.email());
        // AuthenticationManager usa el provider y BCrypt; no se comparan passwords manualmente.
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(UserNotFoundException::new);
        return response(user);
    }

    private AuthenticationResponse response(User user) {
        return new AuthenticationResponse(jwtService.generateToken(user), user.getId(), user.getRole());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
