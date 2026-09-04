package com.uade.tpo.foodmarketplace.entity.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderItemRequest {
    @NotNull
    private Long platoId;

    @NotNull
    @Positive
    private Integer cantidad;
}
