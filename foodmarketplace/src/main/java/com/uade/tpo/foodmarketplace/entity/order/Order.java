package com.uade.tpo.foodmarketplace.entity.order;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

import com.uade.tpo.foodmarketplace.entity.domicilio.Domicilio;
import com.uade.tpo.foodmarketplace.entity.pago.Pago;
import com.uade.tpo.foodmarketplace.entity.user.User;

@Entity
@Data
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioFinal;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "domicilio_entrega_id", nullable = false)
    private Domicilio domicilioEntrega;

    @Column(nullable = false)
    private boolean pagoBloqueado = false;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubPedidoChef> subPedidos = new ArrayList<>();

    @OneToMany(mappedBy = "pedido")
    private List<Pago> pagos = new ArrayList<>();
}
