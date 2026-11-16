package com.uade.tpo.foodmarketplace.repository.favorito;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uade.tpo.foodmarketplace.entity.favorito.Favorito;
import com.uade.tpo.foodmarketplace.entity.plato.EstadoPlato;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    boolean existsByUsuarioIdAndPlatoId(Long usuarioId, Long platoId);

    Optional<Favorito> findByUsuarioIdAndPlatoId(Long usuarioId, Long platoId);

    /** Favoritos del usuario con plato visible, del más nuevo al más viejo. */
    @Query("""
            select f from Favorito f
            where f.usuario.id = :usuarioId and f.plato.estado in :estados
            order by f.fechaCreacion desc, f.id desc
            """)
    List<Favorito> findVisiblesByUsuarioId(@Param("usuarioId") Long usuarioId,
            @Param("estados") List<EstadoPlato> estados);
}
