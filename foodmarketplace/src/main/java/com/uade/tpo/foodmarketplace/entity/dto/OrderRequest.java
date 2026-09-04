package com.uade.tpo.foodmarketplace.entity.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    @NotNull
    private Long userId;
    @NotNull
    private Long domicilioEntregaId;
    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;
}
