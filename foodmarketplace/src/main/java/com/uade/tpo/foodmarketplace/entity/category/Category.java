package com.uade.tpo.foodmarketplace.entity.category;

import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;


@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_category_description", columnNames = "description"))

public class Category {

    public Category(String description) {
        this.description = description;
    }

    public Category() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

}
