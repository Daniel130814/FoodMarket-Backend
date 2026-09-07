package com.uade.tpo.foodmarketplace.repository.plato;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import com.uade.tpo.foodmarketplace.entity.plato.Plato;
import com.uade.tpo.foodmarketplace.entity.plato.EstadoPlato;

public interface PlatoRepository extends JpaRepository<Plato, Long> {

    List<Plato> findByEstado(EstadoPlato estado);

    Optional<Plato> findByIdAndEstadoIn(Long id, List<EstadoPlato> estados);

    @Query("""
            select distinct p
            from Plato p
            left join p.categorias c
            where p.estado in :estados
              and (:nombre is null or lower(p.nombre) like lower(concat('%', :nombre, '%')))
              and (:categoriaId is null or c.id = :categoriaId)
              and (:precioMin is null or p.precio >= :precioMin)
              and (:precioMax is null or p.precio <= :precioMax)
            """)
    List<Plato> buscarConFiltros(
            @Param("estados") List<EstadoPlato> estados,
            @Param("nombre") String nombre,
            @Param("categoriaId") Long categoriaId,
            @Param("precioMin") BigDecimal precioMin,
            @Param("precioMax") BigDecimal precioMax);

    /**
     * Indica si una categoría está asignada a al menos un plato.
     */
    boolean existsByCategoriasId(Long categoryId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Plato p where p.id = :id")
    java.util.Optional<Plato> findByIdForUpdate(
            @Param("id") Long id
    );
}
