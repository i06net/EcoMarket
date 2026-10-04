package pe.edu.upc.ecomarket.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import pe.edu.upc.ecomarket.models.Rol;
import pe.edu.upc.ecomarket.models.Usuario;
import pe.edu.upc.ecomarket.repository.UsuarioRepository;

@Component
@RequiredArgsConstructor
public class UsuarioActual {

    private final UsuarioRepository usuarioRepository;

    // Devuelve null si la petición no tiene token
    public Usuario buscar() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !(autenticacion.getPrincipal() instanceof UserDetails detalles)) {
            return null;
        }
        return usuarioRepository.findByCorreo(detalles.getUsername()).orElse(null);
    }

    // Lanza 401 si no hay sesión
    public Usuario obtener() {
        Usuario usuario = buscar();
        if (usuario == null) {
            throw new AuthenticationCredentialsNotFoundException("Debes iniciar sesión");
        }
        return usuario;
    }

    public static boolean esAdministrador(Usuario usuario) {
        return usuario != null && Rol.ADMINISTRADOR.equals(usuario.getRol().getNombre());
    }
}
