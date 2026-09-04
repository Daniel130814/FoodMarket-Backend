package com.uade.tpo.foodmarketplace.entity.chefprofile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

import com.uade.tpo.foodmarketplace.entity.user.User;

@Entity
@Data
@Table(name = "chef_profiles")
public class ChefProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 2000)
    private String biografia;

    @Column
    private String especialidad;

    @Column
    private String fotoUrl;

    @Column(length = 2000)
    private String descripcion;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
