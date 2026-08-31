package com.uade.tpo.foodmarketplace.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DomicilioRequest {
    private String calle;
    private String numero;
    private String piso;
    private String departamento;
    private String ciudad;
    private String provincia;
    private String codigoPostal;
    private String indicacionesEntrega;
    private Long usuarioId;
}
