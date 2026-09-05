package com.uade.tpo.foodmarketplace.service.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.uade.tpo.foodmarketplace.entity.dto.auth.AuthenticationRequest;
import com.uade.tpo.foodmarketplace.entity.dto.auth.RegisterRequest;
import com.uade.tpo.foodmarketplace.entity.dto.auth.TipoRegistro;
import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.exceptions.user.UserDuplicateException;
import com.uade.tpo.foodmarketplace.repository.user.UserRepository;
import com.uade.tpo.foodmarketplace.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock AuthenticationManager authenticationManager;

    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        service = new AuthenticationService(userRepository, passwordEncoder, jwtService, authenticationManager);
    }

    @Test
    void registraClienteConPasswordCodificada() {
        RegisterRequest request = new RegisterRequest("Ana", "Perez", "ANA@MAIL.COM", "password1",
                TipoRegistro.CLIENTE);
        when(passwordEncoder.encode("password1")).thenReturn("bcrypt");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt");

        var response = service.register(request);

        assertEquals(Role.CLIENTE, response.role());
        assertEquals("jwt", response.accessToken());
        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user ->
                user.getEmail().equals("ana@mail.com") && user.getPassword().equals("bcrypt")));
        assertNotEquals(request.password(), "bcrypt");
    }

    @Test
    void registraChef() {
        RegisterRequest request = new RegisterRequest("Leo", "Chef", "chef@mail.com", "password1",
                TipoRegistro.CHEF);
        when(passwordEncoder.encode(any())).thenReturn("bcrypt");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt");

        assertEquals(Role.CHEF, service.register(request).role());
    }

    @Test
    void rechazaEmailDuplicado() {
        RegisterRequest request = new RegisterRequest("Ana", "Perez", "ana@mail.com", "password1",
                TipoRegistro.CLIENTE);
        when(userRepository.existsByEmailIgnoreCase("ana@mail.com")).thenReturn(true);

        assertThrows(UserDuplicateException.class, () -> service.register(request));
    }

    @Test
    void autenticaMedianteAuthenticationManager() {
        User user = new User();
        user.setId(3L);
        user.setEmail("ana@mail.com");
        user.setRole(Role.CLIENTE);
        when(userRepository.findByEmailIgnoreCase("ana@mail.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt");

        var response = service.authenticate(new AuthenticationRequest("ANA@MAIL.COM", "password1"));

        assertEquals("jwt", response.accessToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void propagaPasswordIncorrecta() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciales invalidas"));

        assertThrows(BadCredentialsException.class,
                () -> service.authenticate(new AuthenticationRequest("ana@mail.com", "incorrecta")));
    }
}
