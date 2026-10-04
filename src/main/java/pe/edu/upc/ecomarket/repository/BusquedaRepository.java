package pe.edu.upc.ecomarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.ecomarket.models.Busqueda;
import java.util.List;

public interface BusquedaRepository extends JpaRepository<Busqueda, Long> {

    List<Busqueda> findTop20ByUsuarioIdOrderByFechaDesc(Long usuarioId);
}