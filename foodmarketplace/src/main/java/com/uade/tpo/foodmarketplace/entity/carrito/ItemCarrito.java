package com.uade.tpo.foodmarketplace.entity.carrito;

import com.uade.tpo.foodmarketplace.entity.plato.Plato;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

/** Representa una cantidad solicitada; sus precios se calculan desde el plato al responder o comprar. */
@Entity
@Data
@Table(name = "items_carrito", uniqueConstraints = @UniqueConstraint(columnNames = { "carrito_id", "plato_id" }))
public class ItemCarrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "carrito_id", nullable = false)
    private Carrito carrito;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plato_id", nullable = false)
    private Plato plato;

    @Column(nullable = false)
    private Integer cantidad;
}
