package com.uade.tpo.foodmarketplace.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data

public class Product {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column
    private Double priceForUnit;

    @Column
    private String name;

    @Column 
    private String description;

    @ManyToOne
    @JoinColumn(name="category_id",nullable=false)
    private Category category;

    @JoinColumn(name="seller_id",nullable=false)
    @ManyToOne
    private Seller seller;






    
}