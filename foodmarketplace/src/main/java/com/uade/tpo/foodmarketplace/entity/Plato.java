package com.uade.tpo.foodmarketplace.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.CascadeType;
import lombok.Data;

@Entity
@Data
@Table(name = "platos")
public class Plato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPlato estado;

    @Column
    private String imagenUrl;

    @ManyToOne
    @JoinColumn(name = "chef_id", nullable = false)
    private User chef;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer stockDisponible;

    @Version
    private Long version;

    @ManyToMany
    @JoinTable(name = "plato_categories", joinColumns = @JoinColumn(name = "plato_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    private List<Category> categorias = new ArrayList<>();

    @OneToMany(mappedBy = "plato", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlatoIngrediente> ingredientes = new ArrayList<>();
}
