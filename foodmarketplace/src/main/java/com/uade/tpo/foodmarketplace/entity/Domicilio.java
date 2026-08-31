package com.uade.tpo.foodmarketplace.entity;

import org.hibernate.annotations.ValueGenerationType;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data

public class Domicilio {
    @Id
    @ValueGenerationType(stretegy = ValueGenerationType.IDENTITY)
    private Long id;
    

}
