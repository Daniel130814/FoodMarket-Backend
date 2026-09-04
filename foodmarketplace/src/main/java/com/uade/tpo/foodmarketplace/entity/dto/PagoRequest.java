package com.uade.tpo.foodmarketplace.entity.dto;

import com.uade.tpo.foodmarketplace.entity.MedioPago;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoRequest {
    @NotNull private MedioPago medioPago;
    @NotNull private Long pedidoId;
}
