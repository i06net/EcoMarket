package pe.edu.upc.ecomarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.upc.ecomarket.dto.ConteoDTO;
import pe.edu.upc.ecomarket.models.Visita;
import java.time.LocalDateTime;
import java.util.List;

public interface VisitaRepository extends JpaRepository<Visita, Long> {

    void deleteByComercioId(Long comercioId);

    long countByComercioIdAndProductoIsNullAndFechaBetween(
            Long comercioId, LocalDateTime desde, LocalDateTime hasta);

    @Query("""
        SELECT new pe.edu.upc.ecomarket.dto.ConteoDTO(
            p.id, p.nombre, COUNT(v)
        )
        FROM Visita v
        JOIN v.producto p
        WHERE v.comercio.id = ?1
        AND v.fecha BETWEEN ?2 AND ?3
        GROUP BY p.id, p.nombre
        ORDER BY COUNT(v) DESC
        """)
    List<ConteoDTO> contarPorProducto(
            Long comercioId,
            LocalDateTime desde,
            LocalDateTime hasta);

    @Query("""
        SELECT new pe.edu.upc.ecomarket.dto.ConteoDTO(
            MIN(v.id), v.terminoBusqueda, COUNT(v)
        )
        FROM Visita v
        WHERE v.comercio.id = ?1
        AND v.terminoBusqueda IS NOT NULL
        AND v.fecha BETWEEN ?2 AND ?3
        GROUP BY v.terminoBusqueda
        ORDER BY COUNT(v) DESC
        """)
    List<ConteoDTO> contarTerminos(
            Long comercioId,
            LocalDateTime desde,
            LocalDateTime hasta);

    @Query("""
        SELECT COUNT(v)
        FROM Visita v
        JOIN v.producto p
        WHERE p.categoria.id = ?1
        AND v.fecha BETWEEN ?2 AND ?3
        """)
    long contarPorCategoria(
            Long categoriaId,
            LocalDateTime desde,
            LocalDateTime hasta);

    List<Visita> findByComercioIdAndFechaAfter(
            Long comercioId, LocalDateTime desde);

    List<Visita> findByUsuarioIdAndProductoIsNotNull(Long usuarioId);

    @Query("""
        SELECT new pe.edu.upc.ecomarket.dto.ConteoDTO(
            p.id, p.nombre, COUNT(v)
        )
        FROM Visita v
        JOIN v.producto p
        WHERE p.activo = true
        AND p.comercio.estado = pe.edu.upc.ecomarket.models.EstadoComercio.APPROVED
        GROUP BY p.id, p.nombre
        ORDER BY COUNT(v) DESC
        """)
    List<ConteoDTO> productosMasConsultados();
}