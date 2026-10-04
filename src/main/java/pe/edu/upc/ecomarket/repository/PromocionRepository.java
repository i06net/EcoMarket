package pe.edu.upc.ecomarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.ecomarket.models.Promocion;
import java.util.List;

public interface PromocionRepository extends JpaRepository<Promocion, Long> {

    void deleteByComercioId(Long comercioId);
    List<Promocion> findByComercioIdOrderByFechaInicioAsc(Long comercioId);
}
