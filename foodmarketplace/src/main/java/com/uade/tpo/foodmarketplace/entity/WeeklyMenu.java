package com.uade.tpo.foodmarketplace.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "weekly_menus")
public class WeeklyMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column
    private String descripcion;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false)
    private Float precio;

    @Column(nullable = false)
    private Integer stockDisponible;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMenu estado;

    @ManyToOne
    @JoinColumn(name = "chef_id", nullable = false)
    private User chef;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Category categoria;
}
