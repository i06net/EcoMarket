package pe.edu.upc.ecomarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.ecomarket.models.Favorito;
import java.util.List;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    void deleteByComercioId(Long comercioId);
    List<Favorito> findByUsuarioIdOrderByFechaRegistroDesc(Long usuarioId);
    boolean existsByUsuarioIdAndComercioId(Long usuarioId, Long comercioId);
    long countByUsuarioId(Long usuarioId);
}
