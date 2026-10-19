package com.uade.tpo.foodmarketplace.entity.dto.user;

import com.uade.tpo.foodmarketplace.entity.user.Role;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Contiene únicamente el rol que puede asignar un administrador. */
@Data
public class UserRoleUpdateRequest {

    @NotNull
    private Role role;
}
