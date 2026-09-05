package com.uade.tpo.foodmarketplace.entity.dto.domicilio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DomicilioRequest {
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
