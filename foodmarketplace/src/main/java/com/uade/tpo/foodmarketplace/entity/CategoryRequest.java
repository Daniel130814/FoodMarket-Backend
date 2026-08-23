package com.uade.tpo.foodmarketplace.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryRequest {
    private int id;
    private String description;

}
