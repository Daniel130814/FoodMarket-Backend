package com.uade.tpo.foodmarketplace.entity;

import org.hibernate.annotations.ValueGenerationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data

public class Domicilio {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column
    private String calle;

    @Column
    private String numero;

    @Column
    private String ciudad;

    @Column
    private String provincia;

    @Column
    private String pais;

    @Column
    private String codigoPostal;


    

}
