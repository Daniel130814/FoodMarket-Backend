package com.uade.tpo.foodmarketplace.entity.dto;

import com.uade.tpo.foodmarketplace.entity.EstadoPago;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadoPagoRequest {
    @NotNull
    private EstadoPago estado;
}
