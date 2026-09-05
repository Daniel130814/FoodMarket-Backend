package com.uade.tpo.foodmarketplace.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.repository.user.UserRepository;

class AuthenticatedUserServiceTest {

    private final AuthenticatedUserService service = new AuthenticatedUserService(
            org.mockito.Mockito.mock(UserRepository.class));

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
