package com.uade.tpo.foodmarketplace.entity.ingrediente;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import com.uade.tpo.foodmarketplace.entity.plato.PlatoIngrediente;

@Entity
@Getter
@Setter
@Table(name = "ingredientes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_ingrediente_nombre", columnNames = "nombre"),
        @UniqueConstraint(name = "uk_ingrediente_nombre_normalizado", columnNames = "nombre_normalizado") })
public class Ingrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public static final int NOMBRE_MAX_LENGTH = 255;

    @Column(nullable = false, length = NOMBRE_MAX_LENGTH)
    private String nombre;

    /** Nombre sin tildes, en minúsculas y con espacios colapsados; garantiza la unicidad real. */
    @Column(name = "nombre_normalizado", length = NOMBRE_MAX_LENGTH)
    private String nombreNormalizado;

    @Column
    private String descripcion;

    @PrePersist
    @PreUpdate
    void actualizarNombreNormalizado() {
        nombreNormalizado = normalizar(nombre);
    }

    public static String normalizar(String nombre) {
        if (nombre == null) {
            return null;
        }
        String sinTildes = java.text.Normalizer.normalize(nombre, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return sinTildes.trim().replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT);
    }

    @OneToMany(mappedBy = "ingrediente")
    @JsonIgnore
    private java.util.List<PlatoIngrediente> platos;
}
