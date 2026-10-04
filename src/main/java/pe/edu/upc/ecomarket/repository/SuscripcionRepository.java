package pe.edu.upc.ecomarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.ecomarket.models.Suscripcion;
import java.util.List;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {

    void deleteByComercioId(Long comercioId);
    List<Suscripcion> findByComercioIdOrderByFechaFinDesc(Long comercioId);
}
