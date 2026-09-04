package com.uade.tpo.foodmarketplace.entity.dto.chefprofile;

import lombok.Data;

/**
 * Contiene los campos de presentación editables de un perfil de chef.
 */
@Data
public class ChefProfileUpdateRequest {

    private String biografia;
    private String especialidad;
    private String fotoUrl;
    private String descripcion;
}
