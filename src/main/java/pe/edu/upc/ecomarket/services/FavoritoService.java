package pe.edu.upc.ecomarket.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.dto.FavoritoDTO;
import pe.edu.upc.ecomarket.dto.FavoritoRespuestaDTO;
import pe.edu.upc.ecomarket.exceptions.AccesoDenegadoException;
import pe.edu.upc.ecomarket.exceptions.ConflictoException;
import pe.edu.upc.ecomarket.exceptions.RecursoNoEncontradoException;
import pe.edu.upc.ecomarket.models.Comercio;
import pe.edu.upc.ecomarket.models.EstadoComercio;
import pe.edu.upc.ecomarket.models.Favorito;
import pe.edu.upc.ecomarket.models.Usuario;
import pe.edu.upc.ecomarket.repository.FavoritoRepository;
import pe.edu.upc.ecomarket.security.UsuarioActual;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final ComercioService comercioService;
    private final UsuarioActual usuarioActual;

    @Transactional
    public FavoritoRespuestaDTO agregar(FavoritoDTO dto) {

        Usuario usuario = usuarioActual.obtener();

        Comercio comercio =
                comercioService.buscar(dto.getComercioId());

        if (comercio.getEstado() != EstadoComercio.APPROVED) {
            throw new RecursoNoEncontradoException(
                    "Comercio",
                    dto.getComercioId()
            );
        }

        if (favoritoRepository.existsByUsuarioIdAndComercioId(
                usuario.getId(),
                comercio.getId())) {

            throw new ConflictoException(
                    "El comercio ya está en tus favoritos"
            );
        }

        Favorito favorito = new Favorito();
        favorito.setUsuario(usuario);
        favorito.setComercio(comercio);

        return aDTO(
                favoritoRepository.save(favorito)
        );
    }

        @Transactional(readOnly = true)
        public List<FavoritoRespuestaDTO> listar() {

        Long usuarioId = usuarioActual.obtener().getId();

        return favoritoRepository
                .findByUsuarioIdAndComercioEstadoOrderByFechaRegistroDesc(
                        usuarioId,
                        EstadoComercio.APPROVED
                )
                .stream()
                .map(this::aDTO)
                .toList();
    }

    @Transactional
    public void eliminar(Long id) {

        Favorito favorito = favoritoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Favorito",
                                id
                        ));

        Long usuarioId = usuarioActual.obtener().getId();

        if (!favorito.getUsuario().getId().equals(usuarioId)) {
            throw new AccesoDenegadoException(
                    "Solo puedes quitar tus propios favoritos"
            );
        }

        favoritoRepository.delete(favorito);
    }

    private FavoritoRespuestaDTO aDTO(Favorito favorito) {

        return new FavoritoRespuestaDTO(
                favorito.getId(),
                comercioService.aDTO(favorito.getComercio()),
                favorito.getFechaRegistro()
        );
    }
}