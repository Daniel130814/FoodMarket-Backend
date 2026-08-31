package com.uade.tpo.foodmarketplace.entity.dto;

import com.uade.tpo.foodmarketplace.entity.MedioPago;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoRequest {
    private MedioPago medioPago;
    private Long pedidoId;
}
