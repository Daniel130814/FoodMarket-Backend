package com.uade.tpo.foodmarketplace.entity.carrito;

import java.util.ArrayList;
import java.util.List;

import com.uade.tpo.foodmarketplace.entity.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/** Carrito activo único por cliente; no reserva stock ni persiste precios calculados. */
@Entity
@Getter
@Setter
@Table(name = "carritos", uniqueConstraints = @UniqueConstraint(name = "uk_carrito_cliente", columnNames = "cliente_id"))
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private User cliente;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCarrito> items = new ArrayList<>();
}
