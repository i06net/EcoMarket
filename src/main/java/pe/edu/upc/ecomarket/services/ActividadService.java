package pe.edu.upc.ecomarket.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.models.Busqueda;
import pe.edu.upc.ecomarket.models.Comercio;
import pe.edu.upc.ecomarket.models.Producto;
import pe.edu.upc.ecomarket.models.Usuario;
import pe.edu.upc.ecomarket.models.Visita;
import pe.edu.upc.ecomarket.repository.BusquedaRepository;
import pe.edu.upc.ecomarket.repository.VisitaRepository;
import pe.edu.upc.ecomarket.security.UsuarioActual;

@Service
@RequiredArgsConstructor
public class ActividadService {

    private final VisitaRepository visitaRepository;
    private final BusquedaRepository busquedaRepository;
    private final UsuarioActual usuarioActual;

    @Transactional
    public void registrarVisita(
            Comercio comercio,
            Producto producto,
            String terminoBusqueda) {

        Usuario usuario = usuarioActual.buscar();

        if (usuario != null &&
                (UsuarioActual.esAdministrador(usuario) ||
                usuario.getId().equals(comercio.getPropietario().getId()))) {
            return;
        }

        Visita visita = new Visita();
        visita.setComercio(comercio);
        visita.setProducto(producto);
        visita.setUsuario(usuario);
        visita.setTerminoBusqueda(limpiar(terminoBusqueda));

        visitaRepository.save(visita);
    }

    @Transactional
    public void registrarBusqueda(String termino, String tipo) {

        String terminoLimpio = limpiar(termino);

        if (terminoLimpio == null) {
            return;
        }

        Busqueda busqueda = new Busqueda();
        busqueda.setUsuario(usuarioActual.buscar());
        busqueda.setTermino(terminoLimpio);
        busqueda.setTipo(tipo);

        busquedaRepository.save(busqueda);
    }

    private String limpiar(String termino) {

        if (termino == null || termino.isBlank()) {
            return null;
        }

        termino = termino.trim().toLowerCase();

        if (termino.length() > 100) {
            termino = termino.substring(0, 100);
        }

        return termino;
    }
}