package pe.edu.upc.ecomarket.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.ecomarket.dto.AuthRespuestaDTO;
import pe.edu.upc.ecomarket.dto.LoginDTO;
import pe.edu.upc.ecomarket.dto.RegistroDTO;
import pe.edu.upc.ecomarket.exceptions.ConflictoException;
import pe.edu.upc.ecomarket.exceptions.RecursoNoEncontradoException;
import pe.edu.upc.ecomarket.models.Rol;
import pe.edu.upc.ecomarket.models.Usuario;
import pe.edu.upc.ecomarket.repository.RolRepository;
import pe.edu.upc.ecomarket.repository.UsuarioRepository;
import pe.edu.upc.ecomarket.security.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UsuarioService usuarioService;

    @Transactional
    public AuthRespuestaDTO registrar(RegistroDTO dto) {

        String correo = dto.getCorreo().trim().toLowerCase();

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ConflictoException("El correo " + correo + " ya está en uso");
        }

        Rol rol = rolRepository.findByNombre(dto.getRol())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No existe el rol " + dto.getRol()));

        Usuario usuario = new Usuario();

        usuario.setNombres(dto.getNombres().trim());
        usuario.setApellidos(dto.getApellidos().trim());
        usuario.setCorreo(correo);
        usuario.setContrasena(
                passwordEncoder.encode(dto.getContrasena()));
        usuario.setRol(rol);

        usuarioRepository.save(usuario);

        return respuesta(usuario);
    }

    public AuthRespuestaDTO login(LoginDTO dto) {

        String correo = dto.getCorreo().trim().toLowerCase();

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Correo o contraseña incorrectos"));

        if (!passwordEncoder.matches(
                dto.getContrasena(),
                usuario.getContrasena())) {

            throw new BadCredentialsException(
                    "Correo o contraseña incorrectos");
        }

        if (!usuario.isActivo()) {
            throw new DisabledException("La cuenta está desactivada");
        }

        return respuesta(usuario);
    }

    private AuthRespuestaDTO respuesta(Usuario usuario) {
        return new AuthRespuestaDTO(
                jwtUtil.generarToken(usuario),
                "Bearer",
                usuarioService.aDTO(usuario)
        );
    }
}