package pe.edu.upc.ecomarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("""
            SELECT c
            FROM Comercio c
            WHERE c.estado = :estado
            AND (
                LOWER(c.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
                OR LOWER(COALESCE(c.descripcion, '')) LIKE LOWER(CONCAT('%', :texto, '%'))
                OR LOWER(COALESCE(c.direccion, '')) LIKE LOWER(CONCAT('%', :texto, '%'))
                OR LOWER(COALESCE(c.distrito, '')) LIKE LOWER(CONCAT('%', :texto, '%'))
                OR LOWER(COALESCE(c.ciudad, '')) LIKE LOWER(CONCAT('%', :texto, '%'))
            )
            ORDER BY c.nombre
            """)
    List<Comercio> buscarPorTexto(
            @Param("estado") EstadoComercio estado,
            @Param("texto") String texto);
}