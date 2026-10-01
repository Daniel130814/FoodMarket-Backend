package com.uade.tpo.foodmarketplace.entity.ingrediente;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import com.uade.tpo.foodmarketplace.entity.plato.PlatoIngrediente;

@Entity
@Getter
@Setter
@Table(name = "ingredientes", uniqueConstraints = @UniqueConstraint(name = "uk_ingrediente_nombre", columnNames = "nombre"))
public class Ingrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column
    private String descripcion;

    @OneToMany(mappedBy = "ingrediente")
    @JsonIgnore
    private java.util.List<PlatoIngrediente> platos;
}
