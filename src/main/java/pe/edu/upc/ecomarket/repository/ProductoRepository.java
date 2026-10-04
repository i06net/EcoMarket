package pe.edu.upc.ecomarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.upc.ecomarket.models.EstadoComercio;
import pe.edu.upc.ecomarket.models.Producto;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsByCategoriaId(Long categoriaId);
    boolean existsByEcoEtiquetasId(Long ecoEtiquetaId);
    List<Producto> findByComercioId(Long comercioId);
    List<Producto> findByComercioIdAndActivoTrueOrderByNombreAsc(Long comercioId);
        List<Producto> findByComercioIdAndActivoTrueAndNombreContainingIgnoreCaseOrderByNombreAsc(
            Long comercioId, String nombre);
    List<Producto> findByComercioPropietarioIdAndActivoTrueOrderByIdAsc(Long propietarioId);
    long countByCategoriaIdAndActivoTrueAndComercioEstado(
            Long categoriaId, EstadoComercio estado);

    @Query("""
        SELECT DISTINCT p
        FROM Producto p
        LEFT JOIN p.ecoEtiquetas e
        WHERE p.activo = true
        AND p.comercio.estado = pe.edu.upc.ecomarket.models.EstadoComercio.APPROVED
        AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', ?1, '%'))
        AND LOWER(p.categoria.nombre) LIKE LOWER(CONCAT('%', ?2, '%'))
        AND (
            ?3 = ''
            OR (
                e.activa = true
                AND LOWER(e.nombre) LIKE LOWER(CONCAT('%', ?3, '%'))
            )
        )
        ORDER BY p.nombre
        """)
    List<Producto> buscar(
            String nombre,
            String categoria,
            String ecoEtiqueta);
}