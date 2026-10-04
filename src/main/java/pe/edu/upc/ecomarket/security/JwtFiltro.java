package pe.edu.upc.ecomarket.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFiltro extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioDetallesService usuarioDetallesService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String correo = jwtUtil.obtenerCorreo(header.substring(7));
            if (correo != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                autenticar(correo, request);
            }
        }
        filterChain.doFilter(request, response);
    }

    private void autenticar(String correo, HttpServletRequest request) {
        try {
            UserDetails usuario = usuarioDetallesService.loadUserByUsername(correo);
            if (usuario.isEnabled()) {
                var autenticacion = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            }
        } catch (UsernameNotFoundException e) {
            // El usuario del token ya no existe: la petición sigue como anónima
        }
    }
}
