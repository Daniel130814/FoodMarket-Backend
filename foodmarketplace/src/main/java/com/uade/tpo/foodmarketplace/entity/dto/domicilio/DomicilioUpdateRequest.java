package com.uade.tpo.foodmarketplace.entity.dto.domicilio;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Contains the address data a user may modify without changing its owner.
 */
@Data
public class DomicilioUpdateRequest {

    @NotBlank
    private String calle;

    @NotBlank
    private String numero;

    private String piso;
    private String departamento;

    @NotBlank
    private String ciudad;

    @NotBlank
    private String provincia;

    @NotBlank
    private String codigoPostal;

    private String indicacionesEntrega;
}
