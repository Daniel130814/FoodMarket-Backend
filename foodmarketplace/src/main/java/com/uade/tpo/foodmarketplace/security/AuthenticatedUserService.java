package com.uade.tpo.foodmarketplace.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.uade.tpo.foodmarketplace.entity.user.Role;
import com.uade.tpo.foodmarketplace.entity.user.User;
import com.uade.tpo.foodmarketplace.repository.user.UserRepository;

/** Obtiene el usuario desde SecurityContext para no confiar en IDs enviados por el frontend. */
@Service
public class AuthenticatedUserService {

    private final UserRepository userRepository;

    public AuthenticatedUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Se requiere autenticación");
        }
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("El usuario autenticado ya no existe"));
    }

    public boolean isAdmin(User user) {
        return user.getRole() == Role.ADMIN;
    }

    public void requireOwnerOrAdmin(User currentUser, Long ownerId) {
        if (!isAdmin(currentUser) && !currentUser.getId().equals(ownerId)) {
            throw new AccessDeniedException("No tenés acceso a un recurso de otro usuario");
        }
    }
}
