package com.uade.tpo.foodmarketplace.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "detalles_pedido")
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private Float precioUnitario;

    @Column(nullable = false)
    private Float subtotal;

    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    private Order pedido;

    @ManyToOne
    @JoinColumn(name = "menu_semanal_id", nullable = false)
    private WeeklyMenu menuSemanal;
}
