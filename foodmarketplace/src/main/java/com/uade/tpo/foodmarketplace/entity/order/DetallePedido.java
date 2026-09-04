package com.uade.tpo.foodmarketplace.entity.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Data;

import com.uade.tpo.foodmarketplace.entity.plato.Plato;

@Entity
@Data
@Table(name = "detalles_pedido")
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @ManyToOne
    @JoinColumn(name = "subpedido_chef_id", nullable = false)
    private SubPedidoChef subPedidoChef;

    @ManyToOne
    @JoinColumn(name = "plato_id", nullable = false)
    private Plato plato;
}
