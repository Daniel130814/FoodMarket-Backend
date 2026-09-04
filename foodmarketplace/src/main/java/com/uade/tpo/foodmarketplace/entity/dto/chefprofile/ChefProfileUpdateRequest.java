package com.uade.tpo.foodmarketplace.entity.dto.chefprofile;

import lombok.Data;

/**
 * Contains the editable presentation fields of a chef profile.
 */
@Data
public class ChefProfileUpdateRequest {

    private String biografia;
    private String especialidad;
    private String fotoUrl;
    private String descripcion;
}
