package com.uade.tpo.foodmarketplace.entity.dto;

import com.uade.tpo.foodmarketplace.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    private String nombre;
    private String apellido;
    private String email;
    private Role role;
}
