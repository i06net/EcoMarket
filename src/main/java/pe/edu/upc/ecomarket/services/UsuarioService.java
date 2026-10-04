package pe.edu.upc.ecomarket.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.dto.PerfilDTO;
import pe.edu.upc.ecomarket.dto.UsuarioDTO;
import pe.edu.upc.ecomarket.exceptions.RecursoNoEncontradoException;
import pe.edu.upc.ecomarket.exceptions.ReglaNegocioException;
import pe.edu.upc.ecomarket.models.Usuario;
import pe.edu.upc.ecomarket.repository.UsuarioRepository;
import pe.edu.upc.ecomarket.security.UsuarioActual;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioActual usuarioActual;

    public List<UsuarioDTO> listar() {

        return usuarioRepository
                .findAllByOrderByIdAsc()
                .stream()
                .map(this::aDTO)
                .toList();
    }

    public UsuarioDTO miPerfil() {

        return aDTO(
                usuarioActual.obtener()
        );
    }

    @Transactional
    public UsuarioDTO actualizarPerfil(
            PerfilDTO dto) {

        Usuario usuario =
                usuarioActual.obtener();

        usuario.setNombres(
                dto.getNombres().trim()
        );

        usuario.setApellidos(
                dto.getApellidos().trim()
        );

        if (dto.getContrasena() != null
                && !dto.getContrasena().isBlank()) {

            usuario.setContrasena(
                    passwordEncoder.encode(
                            dto.getContrasena()
                    )
            );
        }

        return aDTO(
                usuarioRepository.save(usuario)
        );
    }

    @Transactional
    public UsuarioDTO desactivar(Long id) {

        Usuario usuario =
                usuarioRepository.findById(id)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Usuario",
                                        id
                                ));

        Long usuarioActualId =
                usuarioActual.obtener().getId();

        if (usuario.getId().equals(usuarioActualId)) {

            throw new ReglaNegocioException(
                    "Un administrador no puede desactivar su propia cuenta"
            );
        }

        if (!usuario.isActivo()) {
            throw new ReglaNegocioException("El usuario ya está desactivado");
        }

        usuario.setActivo(false);

        return aDTO(
                usuarioRepository.save(usuario)
        );
    }

    public UsuarioDTO aDTO(Usuario usuario) {

        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getCorreo(),
                usuario.getRol().getNombre(),
                usuario.isActivo(),
                usuario.getFechaRegistro()
        );
    }
}