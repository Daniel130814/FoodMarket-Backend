package com.uade.tpo.foodmarketplace.entity.dto.user;

import com.uade.tpo.foodmarketplace.entity.user.Role;

/**
 * DTO seguro para devolver información pública de un usuario.
 * No expone la entidad JPA para evitar enviar campos sensibles, como password,
 * cuando Spring Security se incorpore en el futuro.
 */
public record UserResponse(
        Long id,
        String nombre,
        String apellido,
        String email,
        Role role) {
}
