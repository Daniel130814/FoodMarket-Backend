package com.uade.tpo.foodmarketplace.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.repository.user.UserRepository;

class AuthenticatedUserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final AuthenticatedUserService service = new AuthenticatedUserService(userRepository);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void obtieneUsuarioActualPorUsername() {
        User user = user(10L, Role.CLIENTE);
        user.setUsername("ana");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        when(userRepository.findByUsernameIgnoreCase("ana")).thenReturn(Optional.of(user));

        assertSame(user, service.getCurrentUser());
        verify(userRepository).findByUsernameIgnoreCase("ana");
    }

    @Test
    void propietarioPuedeAcceder() {
        assertDoesNotThrow(() -> service.requireOwnerOrAdmin(user(10L, Role.CLIENTE), 10L));
    }

    @Test
    void usuarioNoPuedeAccederARecursoAjeno() {
        assertThrows(AccessDeniedException.class,
                () -> service.requireOwnerOrAdmin(user(10L, Role.CHEF), 11L));
    }

    @Test
    void adminPuedeSupervisarRecursoAjeno() {
        assertDoesNotThrow(() -> service.requireOwnerOrAdmin(user(1L, Role.ADMIN), 99L));
    }

    private User user(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}
