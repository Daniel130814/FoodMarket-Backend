package com.uade.tpo.foodmarketplace.entity.dto.chefprofile;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChefProfileRequest {
    @Size(max = 2000, message = "no puede superar los 2000 caracteres")
    private String biografia;
    @Size(max = 100, message = "no puede superar los 100 caracteres")
    private String especialidad;
    @Size(max = 100, message = "no puede superar los 100 caracteres")
    private String nacionalidad;
    private String fotoUrl;
    @Size(max = 2000, message = "no puede superar los 2000 caracteres")
    private String descripcion;
}
