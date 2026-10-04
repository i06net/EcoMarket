package pe.edu.upc.ecomarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.ecomarket.models.Comercio;
import pe.edu.upc.ecomarket.models.EstadoComercio;
import java.util.List;

public interface ComercioRepository extends JpaRepository<Comercio, Long> {

    List<Comercio> findByEstadoOrderByIdAsc(EstadoComercio estado);

    List<Comercio> findByEstadoAndDistritoIgnoreCaseOrderByIdAsc(
            EstadoComercio estado, String distrito);

    List<Comercio> findByPropietarioIdOrderByIdAsc(Long propietarioId);

    List<Comercio> findByEstadoAndLatitudBetweenAndLongitudBetween(
            EstadoComercio estado,
            Double latitudMin,
            Double latitudMax,
            Double longitudMin,
            Double longitudMax);

    List<Comercio> findByEstadoAndNombreContainingIgnoreCaseOrderByNombreAsc(
            EstadoComercio estado, String texto);
}